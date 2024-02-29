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

    /*
    //WE MAY WANT TO DO THIS DIFFERENTLY...
    private void receive(DatagramPacket packet){
        if(packet.getPort() == 0){
            return;
        }

        MessageBase m = new MessageDecoder(packet.getData()).parse();
        //m.setOrigin(packet.getAddress(), packet.getPort());

        m.setOrigin(packet.getAddress(), packet.getPort());
        m.setServer(this);

        // just respond to incoming requests, no need to match them to pending requests
        if(m.getType() == MessageBase.Type.REQ_MSG){
            handleMessage(m);
            return;
        }

        if(m.getType() == MessageBase.Type.RSP_MSG && m.getTransactionID().length != TID_LENGTH){
            byte[] tid = m.getTransactionID();
            System.err.println("Response with invalid Transaction ID length received: ");//HEX PRINT TID

            //ErrorMessage err = new ErrorMessage(mtid, ErrorCode.ServerError.code, "received a response with a transaction id length of "+mtid.length+" bytes, expected [implementation-specific]: "+MTID_LENGTH+" bytes");
            //err.setDestination(msg.getOrigin());
            //sendMessage(err);
            return;
        }

        RPCCall c = calls.get(new ByteWrapper(m.getTransactionID()));

        if(c != null){
            if(c.getRequest().getDestinationIP().equals(m.getOriginIP())){
                if(calls.remove(new ByteWrapper(m.getTransactionID()), c)){
                    m.setAssociatedCall(c);
                    c.response(m);
                    handleMessage(m);
                }

                return;
            }

            /*
            if(m.getType() != MessageBase.Type.ERR_MSG && dh_table.getType() == DHTtype.IPV6_DHT){
                MessageBase err = new ErrorMessage(msg.getMTID(), ErrorCode.GenericError.code, "A request was sent to " + c.getRequest().getDestination() + " and a response with matching transaction id was received from " + msg.getOrigin() + " . Multihomed nodes should ensure that sockets are properly bound and responses are sent with the correct source socket address. See BEPs 32 and 45.");
                err.setDestination(c.getRequest().getDestination());
                sendMessage(err);
            }
            *./

            c.setSocketMismatch();
            c.injectStall();

            return;
        }

        if(m.getType() == MessageBase.Type.RSP_MSG && System.currentTimeMillis()-startTime > 120000){ // 2 MINUTES
            //ErrorMessage err = new ErrorMessage(mtid, ErrorCode.ServerError.code, "received a response message whose transaction ID did not match a pending request or transaction expired");
            //err.setDestination(msg.getOrigin());
            //sendMessage(err);
            return;
        }

        if(m.getType() == MessageBase.Type.ERR_MSG){
            handleMessage(m);
            return;
        }

        System.err.println("Unknown message type: "+m);
    }

    //MAKE SURE WE SET THE PUBLIC IP FOR THE MESSAGE...
    private void handleMessage(MessageBase message){
        if(message.getType() == MessageBase.Type.RSP_MSG && message.getPublicIP() != null){
            routingTable.updatePublicIPConsensus(message.getOriginIP(), message.getPublicIP());
        }

        //dh_table.incomingMessage(msg);
        //msg.apply(dh_table);
    }

    private void call(RPCCall call, byte[] tid){
        call.getRequest().setTransactionID(tid);
        //call.addListener(rpcListener);

        if(!call.knownReachableAtCreationTime()){
            //timeoutFilter.registerCall(call);
        }

        sendPool.offer(new EnqueuedSend(call, call.getRequest()));
    }

    /*
    public void doCall(RPCCall c) {

		MessageBase req = c.getRequest();
		if(req.getServer() == null)
			req.setServer(this);

		enqueueEventConsumers.forEach(callback -> callback.accept(c));

		call_queue.add(c);

		drainTrigger.run();
	}
    *./

    //METHOD TO SEND TO ANOTHER NODE - DHT CAN CALL THIS
    public void sendMessage(MessageBase message){
        if(message.getDestinationIP() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        sendPool.offer(new EnqueuedSend(null, message));
    }

    public void send(EnqueuedSend es){
        try{
            byte[] data = es.encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, es.getDestinationIP(), es.getDestinationPort());
            server.send(packet); //MAY BE RESPONSE SO WE NEED TO FIX THIS...

            if(es.getAssociatedCall() != null){ //NOT SURE WHY THIS WOULD EVER OCCUR...
                es.getAssociatedCall().sent(this);
            }

            calls.put(new ByteWrapper(null), es.getAssociatedCall());

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    */

    //WE REALLY JUST NEED TO FIGURE OUT IF HE IS EVEN TAKING INTO ACCOUNT THE PACKETS ORIGIN IP:PORT OR NOT...
    /*
    public void ping(InetAddress address, int port){
        PingRequest pr = new PingRequest();
        pr.setUID(routingTable.getDerivedUID());
        //pr.setTransactionID(); //THIS IS IMPORTANT
        pr.setDestination(address, port);

        new RPCCall(pr);
        //doCall(new RPCCall(pr));
        //SET IP AS MY PUBLIC IP...
        //SET MY PORT

        //SEND THIS SHIT...
    }
    */

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

        MessageDecoder d = new MessageDecoder(packet.getData());

        switch(d.getType()){
            case REQ_MSG:
                if(!requestListeners.isEmpty()){
                    MessageBase m = d.decodeRequest();
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

                call.getMessageCallback().onResponse(call.getMessage(), m);
                break;
        }
    }

    //PROBABLY CHANGE SO THAT WE CAN SET RTT...
    private void send(RPCCall call){
        try{
            if(call instanceof RPCRequestCall){
                call.getMessage().setUID(routingTable.getDerivedUID());
                byte[] tid = generateTransactionID(); //TRY UP TO 5 TIMES TO GENERATE RANDOM - NOT WITHIN CALLS...
                call.getMessage().setTransactionID(tid);
                calls.put(new ByteWrapper(tid), call);
                ((RPCRequestCall) call).sent();
            }

            byte[] data = call.getMessage().encode();
            DatagramPacket packet = new DatagramPacket(data, 0, data.length, call.getMessage().getDestination());

            server.send(packet);

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
