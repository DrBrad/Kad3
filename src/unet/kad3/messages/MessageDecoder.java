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
        if(!ben.containsKey("t") || !ben.containsKey(MessageBase.Type.TYPE_KEY)){
            type = MessageBase.Type.INVALID;
        }
        tid = ben.getBytes("t");
        type = MessageBase.Type.fromRPCTypeName(ben.getString(MessageBase.Type.TYPE_KEY));
    }

    public byte[] getTransactionID(){
        return tid;
    }

    public MessageBase.Type getType(){
        return type;
    }

    public MessageBase decodeRequest(){
        MessageBase message;

        //if(!ben.containsKey(type.getRPCTypeName()) && !ben.containsKey("id")){
        //    return new MessageBase(tid, MessageBase.Method.UNKNOWN, MessageBase.Type.INVALID);
        //}

        MessageBase.Method m = MessageBase.Method.fromRPCName(ben.getString(type.getRPCTypeName()));

        switch(m){
            case PING:
                message = new PingRequest(tid);
                break;

            case FIND_NODE:
                message = new FindNodeRequest(tid);
                ((FindNodeRequest) message).decode(ben.getBencodeObject(type.innerKey()));
                break;

            default:
                return new MessageBase(tid, MessageBase.Method.UNKNOWN, MessageBase.Type.INVALID); //UNKNOWN
        }

        message.setUID(new UID(ben.getBencodeObject(type.innerKey()).getBytes("id")));
        //message.setVersion(ben.getDouble("v"));

        return message;
    }

    public MessageBase decodeResponse(MessageBase.Method method){
        MessageBase message;

        //if(!ben.containsKey("id")){
        //    return new MessageBase(tid, method, MessageBase.Type.INVALID);
        //}

        //try{
        switch(method){
            case PING:
                message = new PingResponse(tid);
                break;

            case FIND_NODE:
                message = new FindNodeResponse(tid);
                ((FindNodeResponse) message).decode(ben.getBencodeObject(type.innerKey()));
                break;

            case GET:

            case PUT:

            default:
                return new MessageBase(tid, method, MessageBase.Type.INVALID);
        }

        message.setUID(new UID(ben.getBencodeObject(type.innerKey()).getBytes("id")));
        if(ben.containsKey("ip")){
            message.setPublic(AddressUtils.unpackAddress(ben.getBytes("ip")));
        }
        //message.setVersion(ben.getDouble("v"));
        //}catch(){
        //    message = new ErrorMessage(tid);
        //}

        return message;
    }

    public MessageBase decodeError(){
        ErrorMessage message = new ErrorMessage(tid);
        message.decode(ben.getBencodeArray(type.innerKey()));

        if(ben.containsKey("ip")){
            message.setPublic(AddressUtils.unpackAddress(ben.getBytes("ip")));
        }
        return message;
    }
}
