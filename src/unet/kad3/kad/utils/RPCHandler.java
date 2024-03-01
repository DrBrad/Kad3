package unet.kad3.kad.utils;

import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.calls.RPCResponseCall;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.UID;

import java.net.InetSocketAddress;

public class RPCHandler {

    public RPCHandler(){

    }

    public void receive(MessageBase message){
        switch(message.getMethod()){
            case PING:
                ping((PingRequest) message);
                break;

            case FIND_NODE:
                findNode((FindNodeRequest) message);
                break;
        }
    }


    private void ping(PingRequest request){
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
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        response.setTransactionID(request.getTransactionID());

        RPCResponseCall call = new RPCResponseCall(response);
        server.sendMessage(call);
    }

    public void ping(InetSocketAddress address, MessageCallback callback){
        PingRequest request = new PingRequest();
        request.setDestination(address);

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(callback);
        server.sendMessage(call);
    }

    private void findNode(FindNodeRequest request){
        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        response.setTransactionID(request.getTransactionID());
        response.addNodes(server.getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));

        RPCResponseCall call = new RPCResponseCall(response);
        server.sendMessage(call);

        //startRefresh();
    }

    public void findNode(InetSocketAddress address, MessageCallback callback, UID target){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(target);

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(callback);
        server.sendMessage(call);
    }
}
