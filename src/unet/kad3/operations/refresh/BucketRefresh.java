package unet.kad3.operations.refresh;

import unet.kad3.messages.ErrorMessage;
import unet.kad3.messages.inter.MessageException;
import unet.kad3.rpc.RPCServer;
import unet.kad3.rpc.calls.RPCRequestCall;
import unet.kad3.operations.inter.Operation;
import unet.kad3.operations.PingOperation;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.util.ArrayList;
import java.util.List;

public class BucketRefresh implements Operation {

    private RPCServer server;
    //private List<Node> queries;

    public BucketRefresh(RPCServer server){
        this.server = server;
        //queries = new ArrayList<>();
    }

    @Override
    public void run(){
        List<Node> queries = new ArrayList<>();

        for(int i = 1; i < UID.ID_LENGTH; i++){
            if(server.getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                final UID k = server.getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                final List<Node> closest = server.getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(!closest.isEmpty()){
                    for(Node n : closest){
                        FindNodeRequest request = new FindNodeRequest();
                        request.setDestination(n.getAddress());
                        request.setTarget(k);

                        server.send(new RPCRequestCall(request, new MessageCallback(){
                            @Override
                            public void onResponse(MessageBase message){
                                if(!n.getUID().equals(message.getUID())){
                                    return;
                                }

                                n.setSeen();

                                System.out.println("SEEN FN "+message.getOrigin());
                                FindNodeResponse r = (FindNodeResponse) message;

                                //queries.addAll(r.getAllNodes());

                                List<Node> nodes = r.getAllNodes();
                                for(int i = nodes.size()-1; i > -1; i--){
                                    if(queries.contains(nodes.get(i))){
                                        nodes.remove(nodes.get(i));
                                    }
                                }

                                /*
                                //queries.addAll(nodes);
                                for(Node n : r.getAllNodes()){
                                    server.getRoutingTable().insert(n);
                                    n.markStale();
                                }

                                new PingOperation(server, r.getAllNodes()).run();
                                */
                                queries.addAll(nodes);

                                new PingOperation(server, nodes).run();
                            }

                            @Override
                            public void onErrorResponse(ErrorMessage message){
                                if(!n.getUID().equals(message.getUID())){
                                    return;
                                }

                                n.setSeen();
                                System.err.println("Node sent error message: "+message.getErrorType().getCode()+" - "+message.getErrorType().getDescription());
                            }

                            /*
                            @Override
                            public void onException(MessageException exception){
                                n.setSeen();
                                exception.printStackTrace();
                            }
                            */

                            @Override
                            public void onStalled(){
                                n.markStale();
                                System.err.println("Node stalled: "+n);
                            }
                        }));
                    }
                }
            }
        }

        //System.out.println("PINGING TOTAL: "+queries.size());

        //new PingOperation(server, queries).run();
    }
}
