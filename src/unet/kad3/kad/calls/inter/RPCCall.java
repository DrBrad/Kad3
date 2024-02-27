package unet.kad3.kad.calls.inter;

import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;

public class RPCCall {

    private MessageBase message;

    public RPCCall(MessageBase message){
        this.message = message;
    }

    public MessageBase getMessage(){
        return message;
    }


    /*
    private MessageBase req;
    private MessageCallback callback;
    private long sentTime = -1, responseTime = -1, expectedRTT = -1;

    public RPCCall(MessageBase message, MessageCallback callback){
        req = message;
        this.callback = callback;
    }

    public MessageBase getRequest(){
        return req;
    }

    public void setResponse(MessageBase message){
        responseTime = System.currentTimeMillis();
        callback.onResponse(req, message);
    }

    public MessageBase getResponse(){
        return req;
    }

    public void sent(){
        sentTime = System.currentTimeMillis();
    }

    public long getSentTime(){
        return sentTime;
    }

    public long getResponseTime(){
        return responseTime;
    }
    */

    /*
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

    public void injectStall(){
        stateTransition(EnumSet.of(RPCState.SENT), RPCState.STALLED);
    }

    public void sent(RPCServer server){
        sentTime = System.currentTimeMillis();

        stateTransition(EnumSet.of(RPCState.UNSENT), RPCState.SENT);

        //scheduler = server.getDHT().getScheduler();

        // spread out the stalls by +- 1ms to reduce lock contention
        //int smear = ThreadLocalRandom.current().nextInt(-1000, 1000);
        //timeoutTimer = scheduler.schedule(this::checkStallOrTimeout, expectedRTT*1000+smear, TimeUnit.MICROSECONDS);
    }

    private synchronized void stateTransition(EnumSet<RPCState> expected, RPCState newState){
        RPCState oldState = state;

        if(!expected.contains(oldState)){
            return;
        }

        state = newState;

        switch(newState){
            case TIMEOUT:
                //DHT.logDebug("RPCCall timed out ID: " + prettyPrint(reqMsg.getMTID()));
                break;

            case ERROR:
            case RESPONDED:
                responseTime = System.currentTimeMillis();
                break;

            case SENT:
                break;

            case STALLED:
                break;

            case UNSENT:
                break;

            default:
                break;
        }

        /*
        for(int i = 0; i < listeners.size(); i++){
            RPCCallListener l = listeners.get(i);
            l.stateTransition(this, oldState, newState);

            switch(newState) {
                case TIMEOUT:
                    l.onTimeout(this);
                    break;

                case STALLED:
                    l.onStall(this);
                    break;

                case RESPONDED:
                    l.onResponse(this, rspMsg);
                    break;
            }
        }
        *./
    }

    public enum RPCState {
        UNSENT,
        SENT,
        STALLED,
        ERROR,
        TIMEOUT,
        RESPONDED
    }
    */
}
