package unet.kad3.kad;

import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.calls.inter.RPCCall;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.ByteWrapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 20, TID_LENGTH = 6;

    private DatagramSocket server;
    private final ConcurrentLinkedQueue<DatagramPacket> receivePool;
    private final ConcurrentLinkedQueue<RPCCall> sendPool;

    private final ConcurrentHashMap<ByteWrapper, RPCCall> calls;
    private final List<RequestListener> requestListeners;
    private final RoutingTable routingTable;

    public RPCServer(RoutingTable routingTable){
        this.routingTable = routingTable;
        //this.dht = dht;
        receivePool = new ConcurrentLinkedQueue<>();
        sendPool = new ConcurrentLinkedQueue<>();
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        requestListeners = new ArrayList<>();

        //routingTable.deriveUID(); //NOT SURE IF THIS WILL FAIL WHEN ITS EMPTY
    }

    public void start(int port)throws SocketException {
        //We add the packet to a pool so that we can read in a different thread so that we don't affect the RPC thread
        //without doing 2 threads we may clog up the RPC server and miss packets
        if(server != null){
            throw new IllegalArgumentException("Server has already started.");
        }

        server = new DatagramSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        if(packet != null){
                            receivePool.offer(packet);
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    if(!receivePool.isEmpty()){
                        receive(receivePool.poll());
                    }

                    if(!sendPool.isEmpty()){
                        send(sendPool.poll());
                    }

                    //CLEAR CALLS THAT ARE PAST RTT...
                }
            }
        }).start();
    }

    public void stop(){
        server.close();
    }

    public boolean isRunning(){
        return (server != null && !server.isClosed());
    }

    public int getPort(){
        return (server != null) ? server.getLocalPort() : 0;
    }

    public void addRequestListener(RequestListener listener){
        requestListeners.add(listener);
    }

    public void removeRequestListener(RequestListener listener){
        requestListeners.remove(listener);
    }

    public RoutingTable getRoutingTable(){
        return routingTable;
    }

    private void receive(DatagramPacket packet){
        if(packet.getPort() == 0){
            return;
        }

        //try{
        MessageDecoder d = new MessageDecoder(packet.getData());

        switch(d.getType()){
            case REQ_MSG:
                if(!requestListeners.isEmpty()){
                    MessageBase m = d.decodeRequest();
                    if(m == null){ // DONT DO THIS CHECK LATER ON...
                        return;
                    }
                    m.setOrigin(packet.getAddress(), packet.getPort());

                    for(RequestListener listener : requestListeners){
                        listener.onRequest(m);
                    }
                }
                break;

            case RSP_MSG:
                ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                if(!calls.containsKey(tid)){
                    return;
                }

                RPCRequestCall call = (RPCRequestCall) calls.get(tid);
                calls.remove(tid);
                MessageBase m = d.decodeResponse(call.getMessage().getMethod());
                m.setOrigin(packet.getAddress(), packet.getPort());

                if(m.getPublic() != null){
                    routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                }

                received++;
                System.out.println(received+"  "+sent);
                call.getMessageCallback().onResponse(call.getMessage(), m);
                break;
        }
        /*
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(new String(packet.getData()));
        }
        */
    }

    private int sent = 0, received = 0;
    //PROBABLY CHANGE SO THAT WE CAN SET RTT...
    private void send(RPCCall call){
        try{
            sent++;
            call.getMessage().setUID(routingTable.getDerivedUID());

            if(call instanceof RPCRequestCall){
                byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                call.getMessage().setTransactionID(tid);
                calls.put(new ByteWrapper(tid), call);
                ((RPCRequestCall) call).sent();
            }

            //try{
            byte[] data = call.getMessage().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, call.getMessage().getDestination());

            server.send(packet);
            /*
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(call.getMessage());
            }
            */

        }catch(IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(RPCCall call){
        if(call.getMessage().getDestination() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        sendPool.offer(call);
    }

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID()throws NoSuchAlgorithmException {
        byte[] tid = new byte[TID_LENGTH];
        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
        r.nextBytes(tid);
        return tid;
    }

    public interface RequestListener {

        void onRequest(MessageBase message);
    }
}
