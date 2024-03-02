package unet.kad3.rpc;

import unet.kad3.operations.refresh.BucketRefresh;
import unet.kad3.operations.refresh.StaleRefresh;
import unet.kad3.rpc.calls.RPCRequestCall;
import unet.kad3.rpc.calls.inter.RPCCall;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.ByteWrapper;
import unet.kad3.utils.UID;
import unet.kad3.utils.net.AddressUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 512, TID_LENGTH = 6;

    private DatagramSocket server;
    private final ConcurrentLinkedQueue<DatagramPacket> receivePool;
    //private final ConcurrentLinkedQueue<MessageBase> sendPool;

    private final ConcurrentHashMap<ByteWrapper, RPCRequestCall> calls;
    /*private final LinkedHashMap<ByteWrapper, RPCRequestCall> calls  = new LinkedHashMap<>(512, 0.75f, true){
        @Override
        protected boolean removeEldestEntry(Map.Entry<ByteWrapper, RPCRequestCall> eldest){
            return (size() > 512);
        }
    };*/

    //private final List<RequestListener> requestListeners;
    protected final RoutingTable routingTable;
    protected final RPCReceiver receiver;

    public RPCServer(RoutingTable routingTable){
        this.routingTable = routingTable;
        receiver = new RPCReceiver(this);
        //this.dht = dht;
        receivePool = new ConcurrentLinkedQueue<>();
        //sendPool = new ConcurrentLinkedQueue<>();
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        //requestListeners = new ArrayList<>();

        //routingTable.deriveUID(); //NOT SURE IF THIS WILL FAIL WHEN ITS EMPTY

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

        try{
            MessageDecoder d = new MessageDecoder(packet.getData());

            switch(d.getType()){
                case REQ_MSG: {
                        /*
                        if(requestListeners.isEmpty()){
                            return;
                        }
                        */
                        MessageBase m = d.decodeRequest();
                        if(m == null){ // DONT DO THIS CHECK LATER ON...
                            return;
                        }

                        m.setOrigin(packet.getAddress(), packet.getPort());

                        receiver.onRequest(m);
                        /*
                        for(RequestListener listener : requestListeners){
                            listener.onRequest(m);
                        }
                        */
                    }
                    break;

                case RSP_MSG: {
                        ByteWrapper tid = new ByteWrapper(d.getTransactionID());

                        if(!calls.containsKey(tid)){
                            return;
                        }

                        RPCRequestCall call = calls.get(tid);
                        calls.remove(tid);
                        MessageBase m = d.decodeResponse(call.getMessage().getMethod());
                        m.setOrigin(packet.getAddress(), packet.getPort());

                        //ENSURE RESPONSE IS ADDRESS IS ACCURATE...
                        if(!packet.getAddress().equals(call.getMessage().getDestinationAddress()) ||
                                packet.getPort() != call.getMessage().getDestinationPort()){
                            return;
                        }

                        if(m.getPublic() != null){
                            routingTable.updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                        }

                        call.getMessageCallback().onResponse(m);
                    }
                    break;
            }
        }catch(IllegalArgumentException e){
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
                    calls.put(new ByteWrapper(tid), (RPCRequestCall) call);
                    ((RPCRequestCall) call).sent();
                    break;
            }

            byte[] data = call.getMessage().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, call.getMessage().getDestination());

            server.send(packet);
        }catch(IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    private void removeStalled(){
        long now = System.currentTimeMillis();
        for(ByteWrapper tid : calls.keySet()){
            RPCRequestCall call = calls.get(tid);
            if(!call.isStalled(now)){
                continue;
            }

            calls.remove(tid);

            //call.getMessageCallback().onResponse(call.getMessage());
        }
    }

    //DONT INIT EVERY TIME...
    private byte[] generateTransactionID()throws NoSuchAlgorithmException {
        byte[] tid = new byte[TID_LENGTH];
        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
        r.nextBytes(tid);
        return tid;
    }

    /*
    public interface RequestListener {

        void onRequest(MessageBase message);
    }
    */
}
