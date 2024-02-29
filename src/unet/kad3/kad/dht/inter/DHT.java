package unet.kad3.kad.dht.inter;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.calls.RPCRequestCall;
import unet.kad3.kad.calls.RPCResponseCall;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DHT implements RPCServer.RequestListener {

    public static final int THREAD_POOL_SIZE = 3;
    public static final long BUCKET_REFRESH_TIME = 3600000;

    private Timer refreshTimer;
    private TimerTask refreshTimerTask;
    private RPCServer server;

    public DHT(RPCServer server){
        this.server = server;
    }

    public void start(){
        server.addRequestListener(this);

        //WE CAN START BY ATTEMPTING UPnP TO GET EXTERNAL IP OTHERWISE CONSENSUS IS NEEDED...

        startRefresh();
    }

    public void stop(){
        server.removeRequestListener(this);
        stopRefresh();
    }

    private void startRefresh(){
        if(refreshTimer == null && refreshTimerTask == null){
            refreshTimer = new Timer(true);
            refreshTimerTask = new TimerTask(){
                @Override
                public void run(){
                    for(int i = 1; i < UID.ID_LENGTH; i++){
                        if(server.getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                            final UID k = server.getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                            final List<Node> closest = server.getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                            if(!closest.isEmpty()){
                                for(Node n : closest){
                                    findNode(n, new MessageCallback(){
                                        @Override
                                        public void onResponse(MessageBase request, MessageBase response){
                                            System.out.println(response.toString());
                                        }
                                    }, k);
                                }
                            }
                        }
                    }

                    /*
                    for(int i = 1; i < KID.ID_LENGTH; i++){
                        if(routingTable.getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                            final KID k = routingTable.getLocal().getKID().generateNodeIdByDistance(i);

                            final List<Node> closest = routingTable.findClosest(k, KBucket.MAX_BUCKET_SIZE);
                            if(!closest.isEmpty()){
                                exe.submit(new Runnable(){
                                    @Override
                                    public void run(){
                                        new NodeLookupMessage(routingTable, closest, k).execute();
                                    }
                                });
                            }
                        }
                    }

                    exe.submit(new Runnable(){
                        @Override
                        public void run(){
                            List<Contact> contacts = routingTable.getAllUnqueriedNodes();
                            if(!contacts.isEmpty()){
                                for(Contact c : contacts){
                                    new PingMessage(routingTable, c.getNode()).execute();
                                }
                            }
                        }
                    });

                    storage.evict();

                    final List<String> data = storage.getRenewal();
                    if(!data.isEmpty()){
                        for(final String r : data){
                            exe.submit(new Runnable(){
                                @Override
                                public void run(){
                                    try{
                                        new StoreMessage(KademliaNode.this, r).execute();
                                    }catch(NoSuchAlgorithmException e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                    */
                }
            };

            refreshTimer.schedule(refreshTimerTask, 0, BUCKET_REFRESH_TIME); //MAKE DELAY LONG, HOWEVER PERIOD AROUND 1 HOUR
        }
    }

    private void stopRefresh(){
        refreshTimerTask.cancel();
        refreshTimer.cancel();
        refreshTimer.purge();
    }

    public UID getUID(){
        return server.getRoutingTable().getDerivedUID();
    }

    //WE PROBABLY WANT TO SET THE SERVER SOMEHOW...

    @Override
    public void onRequest(MessageBase message){
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

    public void ping(Node node, MessageCallback callback){
        PingRequest request = new PingRequest();
        request.setDestination(node.getAddress());

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
    }

    public void findNode(Node node, MessageCallback callback, UID target){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(node.getAddress());
        request.setTarget(target);

        RPCRequestCall call = new RPCRequestCall(request);
        call.setMessageCallback(callback);
        server.sendMessage(call);
    }
}
