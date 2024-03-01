package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;
import unet.kad3.utils.net.AddressUtils;

public class MessageDecoder {

    //REQUEST
    /*
    {
        "t":"aa",
        "y":"q",
        "q":"ping",
        "a":{
            "id":"abcdefghij0123456789"
        }
    }
    */

    //RESPONSE
    /*
    {
        "t":"aa",
        "y":"r",
        "r":{
            "id":"mnopqrstuvwxyz123456"
        },
        "ip": bytes
    }
    */

    //WE SOMEHOW SHOULD PASS THE IP AND PORT OF WHO SENT IT...

    private byte[] tid;
    private MessageBase.Type type;
    private BencodeObject ben;

    public MessageDecoder(byte[] b){
        ben = new BencodeObject(b);
        tid = ben.getBytes("t");
        type = MessageBase.Type.fromRPCTypeName(ben.getString("y"));
    }

    public byte[] getTransactionID(){
        return tid;
    }

    public MessageBase.Type getType(){
        return type;
    }

    public MessageBase decodeRequest(){
        MessageBase message;
        MessageBase.Method m = MessageBase.Method.fromRPCName(ben.getString("q"));

        switch(m){
            case PING:
                message = new PingRequest(tid);
                break;

            case FIND_NODE:
                message = new FindNodeRequest(tid);
                ((FindNodeRequest) message).decode(ben.getBencodeObject("a"));
                break;

            case UNKNOWN:
            default:
                return null; //UNKNOWN
        }

        message.setUID(new UID(ben.getBencodeObject("a").getBytes("id")));
        //message.setVersion(ben.getDouble("v"));

        return message;
    }

    public MessageBase decodeResponse(MessageBase.Method method){
        MessageBase message;

        switch(method){
            case PING:
                message = new PingResponse(tid);
                break;

            case FIND_NODE:
                message = new FindNodeResponse(tid);
                ((FindNodeResponse) message).decode(ben.getBencodeObject("r"));
                break;

            case GET:

            case PUT:

            case UNKNOWN:
            default:
                return null; //UNKNOWN
        }

        message.setUID(new UID(ben.getBencodeObject("r").getBytes("id")));
        if(ben.containsKey("ip")){
            message.setPublic(AddressUtils.unpackAddress(ben.getBytes("ip")));
        }
        //message.setVersion(ben.getDouble("v"));

        return message;
    }
}
