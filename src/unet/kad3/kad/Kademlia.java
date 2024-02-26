package unet.kad3.kad;

import unet.kad3.routing.BucketTypes;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

public class Kademlia {

    private RPCServer server;

    public Kademlia(){
        this(BucketTypes.KADEMLIA, 0);
    }

    public Kademlia(int port){
        this(BucketTypes.KADEMLIA, port);
    }

    public Kademlia(String bucketType, int port){
        this(BucketTypes.fromString(bucketType), port);
    }

    public Kademlia(BucketTypes bucketType, int port){
        System.out.println("Starting with bucket type: "+bucketType.value());
        try{
            server = new RPCServer((RoutingTable) bucketType.getRoutingTable().newInstance(), port);
            server.start();
        }catch(IllegalAccessException | InstantiationException e){
            e.printStackTrace();
        }
    }

    public void join(Node n){

    }

    private void bind(int port){

    }

    public void stop(){

    }
}
