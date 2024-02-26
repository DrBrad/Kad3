package unet.kad3.kad;

import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;

public class RPCCall {

    private MessageBase req, res;
    private UID expectedUID;
    private boolean sourceWasKnownReachable, socketMismatch;
    private long sentTime = -1, responseTime = -1, expectedRTT = -1;
    private RPCState state = RPCState.UNSENT;

    public RPCCall(MessageBase message){
        this.req = message;
    }

    public void setExpectedUID(UID uid){
        this.expectedUID = uid;
    }

    public UID getExpectedUID(){
        return expectedUID;
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

    public boolean matchesExpectedID(){
        return expectedUID.equals(res.getUID());
    }

    public void setSocketMismatch(){
        socketMismatch = true;
    }

    public boolean hasSocketMismatch(){
        return socketMismatch;
    }

    public void response(MessageBase message){

    }

    public MessageBase.Method getMessageMethod(){
        return req.getMethod();
    }

    public MessageBase getRequest(){
        return req;
    }

    public MessageBase getResponse(){
        return res;
    }

    public long getRTT(){
        if(sentTime == -1 || responseTime == -1){
            return -1;
        }
        return responseTime - sentTime;
    }

    public long getSentTime(){
        return sentTime;
    }

    public RPCState getState(){
        return state;
    }

    public boolean inFlight(){
        return state != RPCState.TIMEOUT && state != RPCState.RESPONDED;
    }

    public enum RPCState {
        UNSENT,
        SENT,
        STALLED,
        ERROR,
        TIMEOUT,
        RESPONDED
    }
}
