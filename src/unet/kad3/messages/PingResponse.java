package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;

public class PingResponse extends MessageBase {

    public PingResponse(byte[] tid){
        super(tid, Method.PING, Type.RSP_MSG);
    }

    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = new BencodeObject();
        ben.put("id", uid.getBytes());
        return ben;
    }
}
