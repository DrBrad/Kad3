package unet.kad3.kad;

import unet.kad3.kad.utils.RPCHandler;
import unet.kad3.kad.utils.RefreshHandler;
import unet.kad3.kad.utils.refresh.BucketRefresh;
import unet.kad3.kad.utils.refresh.StaleRefresh;
import unet.kad3.kad.utils.refresh.inter.RefreshOperation;
import unet.kad3.routing.BucketTypes;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Kademlia {

    private RPCServer server;
    private RefreshHandler refresh;
    //private DHT dht;

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
        refresh = new RefreshHandler();
        refresh.addOperation(new BucketRefresh(server));
        refresh.addOperation(new StaleRefresh(server));
        //new RPCHandler()
        //dht = new KDHT(server);
    }

    public void join(int localPort, InetAddress address, int port)throws SocketException {
        join(localPort, new InetSocketAddress(address, port));
    }

    public void join(int localPort, Node node)throws SocketException {
        join(localPort, node.getAddress());
    }

    public void join(int localPort, InetSocketAddress address)throws SocketException {
        bind(localPort);
        //dht.join(address);
    }

    public void bind()throws SocketException {
        bind(0);
    }

    public void bind(int port)throws SocketException {
        if(!server.isRunning()){
            server.start(port);
        }

        if(!refresh.isRunning()){
            refresh.start();
        }

        /*
        if(dht != null){
            dht.start();
            return;
        }
        dht = new KDHT(server);
        dht.start();
        */
    }

    public RefreshHandler getRefreshHandler(){
        return refresh;
    }

    /*
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
    */

    public void stop(){
        server.stop();
        refresh.stop();
        /*
        if(dht != null){
            dht.stop();
        }
        */
    }
}
