package unet.kad3.kad;

import unet.kad3.kad.dht.KDHT;
import unet.kad3.kad.dht.inter.DHT;
import unet.kad3.routing.BucketTypes;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.SocketException;

public class Kademlia {

    private RPCServer server;
    private DHT dht;

    //ALLOW DHT SPECIFICATION

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    /*
    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }
    */

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new RPCServer(routingTable);
        //dht = new KDHT(server);
    }

    public void join(InetAddress address, int port){
        //join(new Node(address, port));
    }

    public void join(Node n){
        //NODE LOOKUP
        //JOIN
        //startRefresh();
    }

    public void bind(int port)throws SocketException {
        if(server.isRunning()){
            throw new IllegalArgumentException("Server is already running.");
        }
        server.start(port);

        if(dht != null){
            dht.start();
            return;
        }
        dht = new KDHT(server);
        dht.start();
    }

    public void setDHT(Class<?> c){
        if(DHT.class.isAssignableFrom(c)){
            try{
                Constructor<?> constructor = c.getConstructor(RPCServer.class);
                DHT dht = (DHT) constructor.newInstance(server);

                if(this.dht != null){
                    this.dht.stop();
                }
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
