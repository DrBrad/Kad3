package unet.kad3.kad;

import unet.kad3.messages.inter.MessageBase;

public class RPCCall {

    private MessageBase req, res;
    private RPCState state = RPCState.UNSENT;

    public RPCCall(MessageBase message){
        this.req = message;
    }

    /*
    public RPCState getState(){
        return state;
    }
    */

    public enum RPCState {
        UNSENT,
        SENT,
        STALLED,
        ERROR,
        TIMEOUT,
        RESPONDED
    }
}
