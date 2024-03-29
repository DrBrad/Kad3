package unet.kad3.rpc;

import unet.kad3.messages.ErrorMessage;
import unet.kad3.messages.inter.MessageException;
import unet.kad3.operations.refresh.BucketRefresh;
import unet.kad3.rpc.calls.RPCRequestCall;
import unet.kad3.rpc.calls.inter.RPCCall;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.ByteWrapper;
import unet.kad3.utils.Node;
import unet.kad3.utils.net.AddressUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 512, TID_LENGTH = 6;

    private DatagramSocket server;
    private final ConcurrentLinkedQueue<DatagramPacket> receivePool;

    private final ConcurrentHashMap<ByteWrapper, RPCRequestCall> calls;
    private final ConcurrentLinkedQueue<ByteWrapper> callsOrder;
    private SecureRandom r;
    protected final RoutingTable routingTable;
    protected final RPCReceiver receiver;

    public RPCServer(RoutingTable routingTable){
        this.routingTable = routingTable;
        receiver = new RPCReceiver(this);
        //this.dht = dht;
        receivePool = new ConcurrentLinkedQueue<>();
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        callsOrder = new ConcurrentLinkedQueue<>();

        try{
            r = SecureRandom.getInstance("SHA1PRNG");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                System.out.println("RESTART");
                new BucketRefresh(RPCServer.this).run();

                //ONLY DO THIS IF WE ARE INCLUDING CACHE
                //new StaleRefresh(RPCServer.this).run();
            }
        });
    }

    public void start(int port)throws SocketException {
        //We add the packet to a pool so that we can read in a different thread so that we don't affect the RPC thread
        //without doing 2 threads we may clog up the RPC server and miss packets
        if(isRunning()){
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
                        onReceive(receivePool.poll());
                    }

                    /*
                    if(!sendPool.isEmpty()){
                        //server.send(sendPool.poll());
                    }
                    */

                    removeStalled();
                }
            }
        }).start();
    }

    public void stop(){
        if(!isRunning()){
            throw new IllegalArgumentException("Server is not currently running.");
        }
        server.close();
    }

    public boolean isRunning(){
        return (server != null && !server.isClosed());
    }

    public int getPort(){
        return (server != null) ? server.getLocalPort() : 0;
    }

    /*
    public void addRequestListener(RequestListener listener){
        requestListeners.add(listener);
    }

    public void removeRequestListener(RequestListener listener){
        requestListeners.remove(listener);
    }
    */

    public RoutingTable getRoutingTable(){
        //System.out.println(routingTable.getConsensusExternalAddress().getHostAddress()+"  "+routingTable.getAllNodes().size());
        return routingTable;
    }

    private void onReceive(DatagramPacket packet){
        if(AddressUtils.isBogon(packet.getAddress(), packet.getPort())){
            return;
        }

        //SPAM THROTTLE...

        //CATCH IF NO TID... - MESSAGE IS POINTLESS - IGNORE
        try{
            MessageDecoder d = new MessageDecoder(packet.getData());

            switch(d.getType()){
                case REQ_MSG: {
                    try{
                        MessageBase m = d.decodeRequest();
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        routingTable.insert(new Node(m.getUID(), m.getOrigin()));
                        System.out.println("SEEN RQ: "+new Node(m.getUID(), m.getOrigin()));

                        receiver.onRequest(m);

                    }catch(MessageException e){
                        ErrorMessage m = new ErrorMessage(d.getTransactionID());
                        m.setErrorType(e.getErrorType());
                        m.setDestination(packet.getAddress(), packet.getPort());
                        m.setPublic(packet.getAddress(), packet.getPort());
                        send(new RPCCall(m));
                        //e.printStackTrace();
                    }
                }
                break;

                case RSP_MSG: {
                        ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                        if(!callsOrder.contains(tid)){
                            return;
                        }

                        RPCRequestCall call = calls.get(tid);
                        callsOrder.remove(tid);
                        calls.remove(tid);

                        //ENSURE RESPONSE IS ADDRESS IS ACCURATE...
                        if(!packet.getAddress().equals(call.getMessage().getDestinationAddress()) ||
                                packet.getPort() != call.getMessage().getDestinationPort()){
                            return;
                        }

                    //try{
                        MessageBase m = d.decodeResponse(call.getMessage().getMethod());
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        call.getMessageCallback().onResponse(m);

                    //}catch(MessageException e){
                    //    call.getMessageCallback().onException(e);
                    //}
                }
                break;

                case ERR_MSG: {
                        ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                        if(!callsOrder.contains(tid)){
                            return;
                        }

                        RPCRequestCall call = calls.get(tid);
                        callsOrder.remove(tid);
                        calls.remove(tid);

                        //ENSURE RESPONSE IS ADDRESS IS ACCURATE...
                        if(!packet.getAddress().equals(call.getMessage().getDestinationAddress()) ||
                                packet.getPort() != call.getMessage().getDestinationPort()){
                            return;
                        }

                    //try{
                        ErrorMessage m = d.decodeError();
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        if(m.getPublic() != null){
                            routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        call.getMessageCallback().onErrorResponse(m);

                    //}catch(MessageException e){
                    //    call.getMessageCallback().onException(e);
                    //}
                }
                break;
            }
        }catch(MessageException e){
            //WE CANT TRUST THE MESSAGE - WE SHOULDN'T ACCEPT IF NO TID OR MESSAGE TYPE IS DEFINED
            //RESPONSE MALFORMED SHOULD BE IGNORED... - MAYBE WE SAVE TO ROUTING TABLE...?
            e.printStackTrace();
        }
    }

    public void send(RPCCall call){
        try{
            if(call.getMessage().getDestination() == null){
                throw new IllegalArgumentException("Message destination set to null");
            }

            call.getMessage().setUID(routingTable.getDerivedUID());

            switch(call.getMessage().getType()){
                case REQ_MSG:
                    byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                    call.getMessage().setTransactionID(tid);
                    ((RPCRequestCall) call).sent();
                    ByteWrapper wrapper = new ByteWrapper(tid);
                    callsOrder.add(wrapper);
                    calls.put(wrapper, (RPCRequestCall) call);
                    break;
            }

            byte[] data = call.getMessage().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, call.getMessage().getDestination());

            server.send(packet);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void removeStalled(){
        long now = System.currentTimeMillis();
        //for(int i = calls.size()-1; i > -1; i--){
        //for(ByteWrapper tid : calls.keySet()){
        //for(int i = 0; i < calls.size(); i++){
        for(ByteWrapper tid : callsOrder){
            RPCRequestCall call = calls.get(tid);
            if(!call.isStalled(now)){
                break;
            }

            callsOrder.remove(tid);
            calls.remove(tid);
            call.getMessageCallback().onStalled();

            //call.getMessageCallback().onResponse(call.getMessage());
        }
    }

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID(){
        byte[] tid = new byte[TID_LENGTH];
        r.nextBytes(tid);
        return tid;
    }
}
