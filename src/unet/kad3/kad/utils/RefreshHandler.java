package unet.kad3.kad.utils;

import unet.kad3.kad.utils.refresh.BucketRefresh;
import unet.kad3.kad.utils.refresh.inter.RefreshOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RefreshHandler {

    private Timer refreshTimer;
    private TimerTask refreshTimerTask;
    private List<RefreshOperation> operations;
    private long refreshTime = 3600000;

    public RefreshHandler(){
        operations = new ArrayList<>();
    }

    public boolean isRunning(){
        return (refreshTimer == null && refreshTimerTask == null);
    }

    public void start(){
        if(refreshTimer == null && refreshTimerTask == null){
            refreshTimer = new Timer(true);
            refreshTimerTask = new TimerTask(){
                @Override
                public void run(){
                    System.out.println("STARTING REFRESH");
                    for(RefreshOperation operation : operations){
                        operation.run();
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

            refreshTimer.schedule(refreshTimerTask, 0, refreshTime); //MAKE DELAY LONG, HOWEVER PERIOD AROUND 1 HOUR
        }
    }

    public void stop(){
        if(refreshTimerTask != null){
            refreshTimerTask.cancel();
        }

        if(refreshTimer != null){
            refreshTimer.cancel();
            refreshTimer.purge();
        }
    }

    public long getRefreshTime(){
        return refreshTime;
    }

    public void setRefreshTime(long time){
        refreshTime = time;
    }

    public void addOperation(RefreshOperation operation){
        operations.add(operation);
    }

    public boolean removeOperation(RefreshOperation operation){
        return operations.remove(operation);
    }

    public RefreshOperation getOperation(int i){
        return operations.get(i);
    }

    public boolean containsOperation(RefreshOperation operation){
        return operations.contains(operation);
    }
}
