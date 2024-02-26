package unet.kad3.kad;

import unet.kad3.messages.MessageBase;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.messages.PingRequest;
import unet.kad3.routing.KB.KBucket;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static unet.kad3.messages.MessageBase.TID_LENGTH;

public class RPCServer {

    public static final int MAX_ACTIVE_CALLS = 20;
    private DatagramSocket server;
    private ConcurrentLinkedQueue<DatagramPacket> packetPool = new ConcurrentLinkedQueue<>();
    private Map<byte[], RPCCall> calls; //BYTE WILL NEED TO BE WRAPPED AS IT WONT WORK WITH MAPS FOR KEYS...
    private RoutingTable routingTable;

    public RPCServer(RoutingTable routingTable, int port){
        this.routingTable = routingTable;
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);

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



        RPCCall c = calls.get(m.getTransactionID());
        /*
        // check if this is a response to an outstanding request
        RPCCall c = calls.get(new ByteWrapper(msg.getMTID()));

        // message matches transaction ID and origin == destination
        if(c != null) {
            // we only check the IP address here. the routing table applies more strict checks to also verify a stable port
            if(c.getRequest().getDestination().getAddress().equals(msg.getOrigin().getAddress())) {
                // remove call first in case of exception
                if(calls.remove(new ByteWrapper(msg.getMTID()),c)) {
                    msg.setAssociatedCall(c);
                    c.response(msg);

                    drainTrigger.run();
                    // apply after checking for a proper response
                    handleMessage(msg);
                }

                return;
            }

            // 1. the message is not a request
            // 2. transaction ID matched
            // 3. request destination did not match response source!!
            // 4. we're using random 48 bit MTIDs
            // this happening by chance is exceedingly unlikely

            // indicates either port-mangling NAT, a multhomed host listening on any-local address or some kind of attack
            // -> ignore response

            DHT.logError("mtid matched, socket address did not, ignoring message, request: " + c.getRequest().getDestination() + " -> response: " + msg.getOrigin() + " v:"+ msg.getVersion().map(Utils::prettyPrint).orElse(""));
            if(msg.getType() != MessageBase.Type.ERR_MSG && dh_table.getType() == DHTtype.IPV6_DHT) {
                // this is more likely due to incorrect binding implementation in ipv6. notify peers about that
                // don't bother with ipv4, there are too many complications
                MessageBase err = new ErrorMessage(msg.getMTID(), ErrorCode.GenericError.code, "A request was sent to " + c.getRequest().getDestination() + " and a response with matching transaction id was received from " + msg.getOrigin() + " . Multihomed nodes should ensure that sockets are properly bound and responses are sent with the correct source socket address. See BEPs 32 and 45.");
                err.setDestination(c.getRequest().getDestination());
                sendMessage(err);
            }

            // but expect an upcoming timeout if it's really just a misbehaving node
            c.setSocketMismatch();
            c.injectStall();

            return;
        }

        // a) it's a response b) didn't find a call c) uptime is high enough that it's not a stray from a restart
        // -> did not expect this response
        if (msg.getType() == Type.RSP_MSG && Duration.between(startTime, Instant.now()).getSeconds() > 2*60) {
            byte[] mtid = msg.getMTID();
            DHT.logDebug("Cannot find RPC call for response: "+ Utils.prettyPrint(mtid));
            ErrorMessage err = new ErrorMessage(mtid, ErrorCode.ServerError.code, "received a response message whose transaction ID did not match a pending request or transaction expired");
            err.setDestination(msg.getOrigin());
            sendMessage(err);
            return;
        }

        if (msg.getType() == Type.ERR_MSG) {
            handleMessage(msg);
            return;
        }

        DHT.logError("not sure how to handle message " + msg);
        */



        //handleMessage(m);

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
