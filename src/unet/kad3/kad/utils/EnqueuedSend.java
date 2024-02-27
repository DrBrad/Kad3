package unet.kad3.kad.utils;

import unet.kad3.kad.calls.RPCResponseCall;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingResponse;
import unet.kad3.messages.inter.MessageBase;

import java.net.InetAddress;

public class EnqueuedSend {

    private RPCResponseCall call;
    private MessageBase message;

    public EnqueuedSend(RPCResponseCall call, MessageBase message){
        this.call = call;
        this.message = message;

        if(message.getUID() == null){
            //message.setUID(); //DERIVE UUID < FROM THE RPCServer...
        }

        if((message instanceof PingResponse || message instanceof FindNodeResponse) && message.getPublicIP() == null){
            message.setPublicIP(message.getDestinationIP());
            //message.setPublicPort(message.getDestinationPort());
        }

        if(call != null){
            long configuredRTT = call.getExpectedRTT();

            if(configuredRTT == -1){
                //configuredRTT = timeoutFilter.getStallTimeout();
            }

            call.setExpectedRTT(configuredRTT);
        }
    }

    public boolean hasAssociatedCall(){
        return call != null;
    }

    public RPCResponseCall getAssociatedCall(){
        return call;
    }

    public InetAddress getDestinationIP(){
        return message.getDestinationIP();
    }

    public int getDestinationPort(){
        return message.getDestinationPort();
    }

    public byte[] encode(){
        /*
        try {
            buf.rewind();
            buf.limit(dh_table.getType().MAX_PACKET_SIZE);
            toSend.encode(buf);
        } catch (Exception e) {
            ByteBuffer t = ByteBuffer.allocate(4096);
            try {
                toSend.encode(t);
            } catch(Exception e2) {

            }

            DHT.logError("encode failed for " + toSend.toString() + " 2nd encode attempt: (" + t.limit() + ") bytes. base map was:" + Utils.prettyPrint(toSend.getBase())  );


            throw new IOException(e) ;
        }
        */

        return null;
    }
}
