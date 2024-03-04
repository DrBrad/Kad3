package unet.kad3.operations;

import unet.kad3.messages.ErrorMessage;
import unet.kad3.messages.inter.MessageException;
import unet.kad3.rpc.RPCServer;
import unet.kad3.rpc.calls.RPCRequestCall;
import unet.kad3.rpc.RefreshHandler;
import unet.kad3.operations.inter.Operation;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;
import unet.kad3.utils.Node;

import java.net.InetSocketAddress;

public class JoinOperation implements Operation {

    private RPCServer server;
    private RefreshHandler refresh;
    private InetSocketAddress address;

    public JoinOperation(RPCServer server, RefreshHandler refresh, InetSocketAddress address){
        this.server = server;
        this.refresh = refresh;
        this.address = address;
    }

    @Override
    public void run(){
        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(server.getRoutingTable().getDerivedUID());

        server.send(new RPCRequestCall(request, new MessageCallback(){
            @Override
            public void onResponse(MessageBase message){
                FindNodeResponse r = (FindNodeResponse) message;

                server.getRoutingTable().insert(new Node(r.getUID(), message.getOrigin()));
                System.out.println("SEEN FN "+message.getOrigin());

                new PingOperation(server, r.getAllNodes()).run();

                if(!refresh.isRunning()){
                    refresh.start();
                }
            }

            @Override
            public void onErrorResponse(ErrorMessage message){
                System.err.println("Unable to join node, node sent error message: "+message.getErrorType().getCode()+" - "+message.getErrorType().getDescription());
            }

            /*
            @Override
            public void onException(MessageException exception){
                exception.printStackTrace();
            }
            */

            @Override
            public void onStalled(){
                System.err.println("Unable to join node, node never responded.");
            }
        }));
    }
}
