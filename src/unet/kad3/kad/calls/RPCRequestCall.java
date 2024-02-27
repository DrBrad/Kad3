package unet.kad3.kad.calls;

import unet.kad3.kad.calls.inter.RPCCall;
import unet.kad3.kad.dht.inter.DHT;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.messages.inter.MessageCallback;

public class RPCRequestCall extends RPCCall {

    private MessageCallback callback;
    private long sentTime = -1, responseTime = -1, expectedRTT = -1;

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

    public long getResponseTime(){
        return responseTime;
    }
}
