package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;

public class PingRequest extends MessageBase {

    public PingRequest(){
        super(null, Method.PING, Type.REQ_MSG);
    }

    public PingRequest(byte[] tid){
        super(tid, Method.PING, Type.REQ_MSG);
    }

    //DECODE...

    @Override
    public BencodeObject getBencode(){
        //BencodeObject ben = super.getBencode();
        //ben.getBencodeObject("a").put("id", uid.getBytes());
        return super.getBencode();
    }
}
