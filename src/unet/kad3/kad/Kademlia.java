package unet.kad3.kad;

import unet.kad3.utils.Node;

public class Kademlia {

    private RPCServer server;

    public Kademlia(){
        this(0);
    }

    public Kademlia(int port){
        server = new RPCServer(port);



    }

    public void join(Node n){

    }

    private void bind(int port){

    }

    public void stop(){

    }


}
