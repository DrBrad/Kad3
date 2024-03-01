package unet.kad3.kad.utils;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.calls.RPCResponseCall;
import unet.kad3.kad.calls.inter.RPCCall;
import unet.kad3.messages.*;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.UID;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class RPCHandler {

    /*
    private RPCServer server;

    public RPCHandler(RPCServer server){
        this.server = server;
        server.addRequestListener(this);
    }

    @Override
    public void onRequest(MessageBase request){
        MessageBase response;

        switch(request.getMethod()){
            case PING:
                response = ping((PingRequest) request);
                break;

            case FIND_NODE:
                response = findNode((FindNodeRequest) request);
                break;

            default:
                return;
        }

        response.setDestination(request.getOrigin());
        response.setPublic(request.getOrigin());

        RPCResponseCall call = new RPCResponseCall(response);
        server.sendMessage(call);
    }

    private MessageBase ping(PingRequest request){
        PingResponse response = new PingResponse(request.getTransactionID());
        return response;
    }

    public void ping(InetSocketAddress address, MessageCallback callback){
        PingRequest request = new PingRequest();
        request.setDestination(address);

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(callback);
        sendMessage(call);
    }

    private MessageBase findNode(FindNodeRequest request){
        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
        response.addNodes(routingTable.findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE));
        return response;
    }

    public void findNode(InetSocketAddress address, UID target, MessageCallback callback){
        //FindNodeRequest request = factory.create()
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(target);

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(callback);
        sendMessage(call);
    }
    */
}
