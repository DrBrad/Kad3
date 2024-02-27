package unet.kad3.kad;

import unet.kad3.routing.inter.RoutingTable;

public class RPCServerManager {

    private RoutingTable routingTable;

    public RPCServerManager(RoutingTable routingTable){
        this.routingTable = routingTable;
    }
}
