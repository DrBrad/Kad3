package unet.kad3.kad;

import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;

public class RPCCall {

    private MessageBase req, res;
    private UID expectedUID;
    private boolean sourceWasKnownReachable, socketMismatch;
    private long expectedRTT;
    private RPCState state = RPCState.UNSENT;

    public RPCCall(MessageBase message){
        this.req = message;
    }

    public void setExpectedUID(UID uid){
        this.expectedUID = uid;
    }

    public boolean knownReachableAtCreationTime(){
        return sourceWasKnownReachable;
    }

    public void setExpectedRTT(long rtt){
        expectedRTT = rtt;
    }

    public long getExpectedRTT(){
        return expectedRTT;
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
