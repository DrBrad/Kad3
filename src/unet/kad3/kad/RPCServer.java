package unet.kad3.kad;

import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.messages.PingRequest;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.ByteWrapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static unet.kad3.messages.inter.MessageBase.TID_LENGTH;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 20;
    private DatagramSocket server;
    private long startTime;
    private ConcurrentLinkedQueue<DatagramPacket> packetPool = new ConcurrentLinkedQueue<>();
    private Map<ByteWrapper, RPCCall> calls;
    private RoutingTable routingTable;

    public RPCServer(RoutingTable routingTable, int port){
        this.routingTable = routingTable;
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
        startTime = System.currentTimeMillis();

        try{
            server = new DatagramSocket(port);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void handlePacket(DatagramPacket packet){
        if(packet.getPort() == 0){
            return;
        }

        MessageBase m = new MessageDecoder(packet.getData()).parse();
        //m.setOrigin(packet.getAddress(), packet.getPort());

        if(m.getType() == MessageBase.Type.RSP_MSG && m.getTransactionID().length != TID_LENGTH){
            byte[] tid = m.getTransactionID();
            //DHT.logDebug("response with invalid mtid length received: "+ Utils.prettyPrint(mtid));
            //ErrorMessage err = new ErrorMessage(mtid, ErrorCode.ServerError.code, "received a response with a transaction id length of "+mtid.length+" bytes, expected [implementation-specific]: "+MTID_LENGTH+" bytes");
            //err.setDestination(msg.getOrigin());
            //sendMessage(err);
            return;
        }

        RPCCall c = calls.get(new ByteWrapper(m.getTransactionID()));

        if(c != null){
            if(c.getRequest().getDestinationIP().getAddress().equals(m.getOriginIP().getAddress())){
                if(calls.remove(new ByteWrapper(m.getTransactionID()), c)){
                    //m.setAssociatedCall(c);
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
            */

            c.setSocketMismatch();
            //c.injectStall();

            return;
        }

        if(m.getType() == MessageBase.Type.RSP_MSG && System.currentTimeMillis()-startTime > 2*60*1000){ // 2 MINUTES
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

    //WE REALLY JUST NEED TO FIGURE OUT IF HE IS EVEN TAKING INTO ACCOUNT THE PACKETS ORIGIN IP:PORT OR NOT...
    public void ping(InetAddress address, int port){
        PingRequest pr = new PingRequest();
        //pr.setUID(deriveID);
        //pr.setTransactionID(); //THIS IS IMPORTANT
        pr.setDestination(address, port);
        //SET IP AS MY PUBLIC IP...
        //SET MY PORT

        //SEND THIS SHIT...
    }

    public int getPort(){
        return server.getLocalPort();
    }

    public void start(){
        //We add the packet to a pool so that we can read in a different thread so that we don't affect the RPC thread
        //without doing 2 threads we may clog up the RPC server and miss packets
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);
                        packetPool.offer(packet);
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
                    if(!packetPool.isEmpty()){
                        handlePacket(packetPool.poll());
                    }
                }
            }
        }).start();
    }

    public void stop(){

    }
}
