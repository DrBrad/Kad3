package unet.kad3.kad.calls;

import unet.kad3.kad.calls.inter.RPCCall;
import unet.kad3.messages.inter.MessageBase;

public class RPCResponseCall extends RPCCall {

    public RPCResponseCall(MessageBase message){
        super(message);
    }
}
