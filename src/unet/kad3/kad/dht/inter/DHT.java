package unet.kad3.kad.dht.inter;

import unet.kad3.kad.RPCCall;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.ByteWrapper;
import unet.kad3.utils.Node;

import java.util.concurrent.ConcurrentHashMap;

public class DHT {

    public static final int MAX_ACTIVE_CALLS = 20, THREAD_POOL_SIZE = 3;
    public static final long BUCKET_REFRESH_TIME = 3600000;

    private ConcurrentHashMap<ByteWrapper, MessageBase> calls;

    public DHT(){
        calls = new ConcurrentHashMap<>(MAX_ACTIVE_CALLS);
    }

    //WE PROBABLY WANT TO SET THE SERVER SOMEHOW...

    public void ping(PingRequest request){
        /*
        if (!isRunning()) {
            return;
        }

        // ignore requests we get from ourself
        if (node.isLocalId(r.getID())) {
            return;
        }
        */

        PingResponse response = new PingResponse(request.getTransactionID());
        response.setDestination(request.getOriginIP(), request.getOriginPort());
        //request.getServer().sendMessage(response);

        //node.recieved(r);
    }

    public void ping(Node node){
        PingRequest request = new PingRequest();
        request.setDestination(node.getAddress(), node.getPort());
        //request.setUID(routingTable.getDerivedUID());
        request.setTransactionID(null); //DEFINE THIS...
        calls.put(new ByteWrapper(null), request);
        //server.sendMessage(request);
    }

    public void findNode(FindNodeRequest request){
        /*
        if (!isRunning()) {
            return;
        }

        // ignore requests we get from ourself
        if (node.isLocalId(r.getID())) {
            return;
        }

        AbstractLookupResponse response;
        if(r instanceof FindNodeRequest)
            response = new FindNodeResponse(r.getMTID());
        else
            response = new UnknownTypeResponse(r.getMTID());

        populateResponse(r.getTarget(), response, r.doesWant4() ? DHTConstants.MAX_ENTRIES_PER_BUCKET : 0, r.doesWant6() ? DHTConstants.MAX_ENTRIES_PER_BUCKET : 0);

        response.setDestination(r.getOrigin());
        r.getServer().sendMessage(response);

        node.recieved(r);
        */

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());

        response.setDestination(request.getOriginIP(), request.getOriginPort());
        //request.getServer().sendMessage(response);
    }
}
