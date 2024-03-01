package unet.kad3.kad.utils.operations;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.utils.RefreshHandler;
import unet.kad3.kad.utils.inter.Operation;
import unet.kad3.kad.utils.refresh.BucketRefresh;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.utils.Node;

import java.net.InetSocketAddress;

public class JoinOperation implements Operation {

    private RPCServer server;
    private RefreshHandler refresh;
    private InetSocketAddress address;

    public JoinOperation(RPCServer server, RefreshHandler refresh, InetSocketAddress address){
        this.server = server;
        this.refresh = refresh;
        this.address = address;
    }

    @Override
    public void run(){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(server.getRoutingTable().getDerivedUID());

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(new MessageCallback(){
            @Override
            public void onResponse(MessageBase message){
                FindNodeResponse r = (FindNodeResponse) message;

                for(Node n : r.getAllNodes()){
                    server.getRoutingTable().insert(n);
                }

                //new PingOperation(server, r.getAllNodes()).run();
                //new BucketRefresh(server).run();
                if(!refresh.isRunning()){
                    refresh.start();
                }
            }
        });
        server.send(call);
    }
}
