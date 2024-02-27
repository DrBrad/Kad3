package unet.kad3.kad;

import unet.kad3.kad.dht.inter.DHT;
import unet.kad3.routing.BucketTypes;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

import java.net.InetAddress;

public class Kademlia {

    private RPCServer server;
    private DHT dht;

    //ALLOW DHT SPECIFICATION

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

    public void join(InetAddress address, int port){
        //join(new Node(address, port));
    }

    public void join(Node n){
        //NODE LOOKUP
        //JOIN
        //startRefresh();
    }

    private void bind(int port){

    }

    public void setDHT(DHT dht){
        this.dht = dht;
    }

    public DHT getDHT(){
        return dht;
    }

    public void stop(){
        server.stop();
    }
}
