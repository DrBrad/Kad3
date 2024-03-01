package unet.kad3.kad.utils.operations;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.utils.inter.Operation;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.utils.Node;

import java.util.List;

public class PingOperation implements Operation {

    private RPCServer server;
    private List<Node> nodes;

    public PingOperation(RPCServer server, List<Node> nodes){
        this.server = server;
        this.nodes = nodes;
    }

    @Override
    public void run(){
        for(Node n : nodes){
            PingRequest request = new PingRequest();
            request.setDestination(n.getAddress());

            RPCRequestCall call = new RPCRequestCall(request);
            call.setMessageCallback(new MessageCallback(){
                @Override
                public void onResponse(MessageBase message){
                    n.setSeen();
                }
            });
            server.send(call);
        }
    }
}
