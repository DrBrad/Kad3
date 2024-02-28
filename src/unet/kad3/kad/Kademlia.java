package unet.kad3.kad;

import com.sun.jdi.InvocationException;
import unet.kad3.kad.dht.inter.DHT;
import unet.kad3.routing.BucketTypes;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;

public class Kademlia {

    private RPCServer server;
    private DHT dht;

    //ALLOW DHT SPECIFICATION

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable(), 0);
    }

    public Kademlia(int port){
        this(BucketTypes.KADEMLIA.getRoutingTable(), port);
    }

    public Kademlia(String bucketType, int port){
        this(BucketTypes.fromString(bucketType).getRoutingTable(), port);
    }

    public Kademlia(RoutingTable routingTable, int port){
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new RPCServer(routingTable, port);
        server.start();
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

    public void setDHT(Class<?> c){
        if(DHT.class.isAssignableFrom(c)){
            try{
                Constructor<?> constructor = c.getConstructor(RPCServer.class);
                dht = (DHT) constructor.newInstance(server);
                dht.start();

            }catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }

    public DHT getDHT(){
        return dht;
    }

    public void stop(){
        server.stop();
        if(dht != null){
            dht.stop();
        }
    }
}
