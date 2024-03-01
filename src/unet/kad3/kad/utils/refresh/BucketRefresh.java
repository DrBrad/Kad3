package unet.kad3.kad.utils.refresh;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.utils.inter.Operation;
import unet.kad3.kad.utils.operations.PingOperation;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.util.List;

public class BucketRefresh implements Operation {

    private RPCServer server;

    public BucketRefresh(RPCServer server){
        this.server = server;
    }

    @Override
    public void run(){
        for(int i = 1; i < UID.ID_LENGTH; i++){
            if(server.getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                final UID k = server.getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                final List<Node> closest = server.getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(!closest.isEmpty()){
                    for(Node n : closest){
                        FindNodeRequest request = new FindNodeRequest();
                        request.setDestination(n.getAddress());
                        request.setTarget(k);

                        RPCRequestCall call = new RPCRequestCall(request);
                        call.setMessageCallback(new MessageCallback(){
                            @Override
                            public void onResponse(MessageBase message){
                                n.setSeen();
                                FindNodeResponse r = (FindNodeResponse) message;

                                for(Node n : r.getAllNodes()){
                                    server.getRoutingTable().insert(n);
                                }

                                new PingOperation(server, r.getAllNodes()).run();
                            }
                        });
                        server.send(call);
                    }
                }
            }
        }
    }
}
