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
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

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
        response.setDestination(request.getOriginIP(), request.getOriginPort());

        RPCResponseCall call = new RPCResponseCall(response);
        server.sendMessage(call);
    }

    public void ping(Node node, MessageCallback callback){
        PingRequest request = new PingRequest();
        request.setDestination(node.getAddress(), node.getPort());
        sendMessage(request, callback);
    }

    private void findNode(FindNodeRequest request){
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

    public void findNode(Node node, MessageCallback callback){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(node.getAddress(), node.getPort());
        sendMessage(request, callback);
    }

    private void sendMessage(MessageBase message, MessageCallback callback){
        RPCRequestCall call = new RPCRequestCall(message);
        call.setMessageCallback(callback);
        server.sendMessage(call);
    }
}
