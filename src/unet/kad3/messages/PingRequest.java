package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;

public class PingRequest extends MessageBase {

    public PingRequest(){
        super(null, Method.PING, Type.REQ_MSG);
    }

    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = new BencodeObject();
        ben.put("id", uid.getBytes());
        return ben;
    }
}
