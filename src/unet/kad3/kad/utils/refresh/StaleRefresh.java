package unet.kad3.kad.utils.refresh;

import unet.kad3.kad.RPCServer;
import unet.kad3.kad.utils.refresh.inter.RefreshOperation;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.utils.Node;

import java.util.List;

public class StaleRefresh implements RefreshOperation {

    private RPCServer server;

    public StaleRefresh(RPCServer server){
        this.server = server;
    }

    @Override
    public void run(){
        List<Node> nodes = server.getRoutingTable().getAllUnqueriedNodes();
        System.out.println("NODES: "+nodes.size());
        if(!nodes.isEmpty()){
            for(Node n : nodes){
                ping(n.getAddress(), new MessageCallback(){
                    @Override
                    public void onResponse(MessageBase request, MessageBase response){
                        n.setSeen();
                    }
                });
            }
        }
    }
}
