package unet.kad3.rpc.calls;

import unet.kad3.rpc.calls.inter.RPCCall;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;

public class RPCRequestCall extends RPCCall {

    private MessageCallback callback;
    private long sentTime = -1;
    public static final long STALLED_TIME = 60000;

    public RPCRequestCall(MessageBase message){
        super(message);
    }

    public void setMessageCallback(MessageCallback callback){
        this.callback = callback;
    }

    public MessageCallback getMessageCallback(){
        return callback;
    }

    public void sent(){
        sentTime = System.currentTimeMillis();
    }

    public long getSentTime(){
        return sentTime;
    }

    public boolean isStalled(long now){
        return (now-sentTime > STALLED_TIME);
    }
}
