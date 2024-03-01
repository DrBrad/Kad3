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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DHT /*implements RPCServer.RequestListener*/ {

    //DONT DO DHT AS SEPERATE CLASS CHANGE THIS...

    //public static final int THREAD_POOL_SIZE = 3;
    private RPCServer server;
/*
    public DHT(RPCServer server){
        this.server = server;
        //WE CAN START BY ATTEMPTING UPnP TO GET EXTERNAL IP OTHERWISE CONSENSUS IS NEEDED...
    }

    public void start(){
        server.addRequestListener(this);
    }

    public void join(InetSocketAddress address){
        findNode(address, new MessageCallback(){
            @Override
            public void onResponse(MessageBase request, MessageBase response){
                //System.out.println(response.getBencode());
                FindNodeResponse r = (FindNodeResponse) response;

                for(Node n : r.getAllNodes()){
                    server.getRoutingTable().insert(n);
                    /*
                    ping(n.getAddress(), new MessageCallback(){
                        @Override
                        public void onResponse(MessageBase request, MessageBase response){
                            server.getRoutingTable().insert(n);
                        }
                    });
                    *./
                    //System.out.println(n);
                }

                //CHECK IF PING CALLS ARE STILL ON-GOING...

                //startRefresh();
            }
        }, server.getRoutingTable().getDerivedUID());

    }

    public void stop(){
        server.removeRequestListener(this);
        //stopRefresh();
    }

    public UID getUID(){
        System.out.println(server.getRoutingTable().getConsensusExternalAddress());
        return server.getRoutingTable().getDerivedUID();
    }

    //WE PROBABLY WANT TO SET THE SERVER SOMEHOW...

    @Override
    public void onRequest(MessageBase message){
    }*/
}
