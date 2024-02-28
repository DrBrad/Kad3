package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;

public class PingResponse extends MessageBase {

    public PingResponse(byte[] tid){
        super(tid, Method.PING, Type.RSP_MSG);
    }

    @Override
    public BencodeObject getBencode(){
        return super.getBencode();
    }
}
