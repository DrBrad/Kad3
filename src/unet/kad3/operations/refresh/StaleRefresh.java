package unet.kad3.operations.refresh;

import unet.kad3.rpc.RPCServer;
import unet.kad3.operations.inter.Operation;
import unet.kad3.operations.PingOperation;
import unet.kad3.utils.Node;

import java.util.List;

public class StaleRefresh implements Operation {

    private RPCServer server;

    public StaleRefresh(RPCServer server){
        this.server = server;
    }

    @Override
    public void run(){
        List<Node> nodes = server.getRoutingTable().getAllUnqueriedNodes();
        if(nodes.isEmpty()){
            return;
        }
        new PingOperation(server, nodes).run();
    }
}
