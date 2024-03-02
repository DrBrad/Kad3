package unet.kad3.rpc;

import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.rpc.calls.inter.RPCCall;

public class RPCReceiver {

    private RPCServer server;

    public RPCReceiver(RPCServer server){
        this.server = server;
    }

    public void onRequest(MessageBase message){
        MessageBase response;
        switch(message.getMethod()){
            case PING:
                response = onPing((PingRequest) message);
                break;

            case FIND_NODE:
                response = onFindNode((FindNodeRequest) message);
                break;

            default:
                return;
        }

        server.send(new RPCCall(response));
    }

    private PingResponse onPing(PingRequest request){
        PingResponse response = new PingResponse(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        return response;
    }

    private FindNodeResponse onFindNode(FindNodeRequest request){
        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());
        response.addNodes(server.getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
        return response;
    }
}
