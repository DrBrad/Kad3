package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;

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
        }
    }
    */

    //WE SOMEHOW SHOULD PASS THE IP AND PORT OF WHO SENT IT...

    private byte[] tid;
    private byte[] b;

    public MessageDecoder(byte[] b){
        this.b = b;
    }

    public MessageBase parse(){
        BencodeObject ben = new BencodeObject(b);

        tid = ben.getBytes("t");
        MessageBase.Type t = MessageBase.Type.valueOf("");//ben.getString("y"));

        //VERSION...
        //ben.getString("v");

        switch(t){
            case REQ_MSG:
                return decodeRequest(ben);

            case RSP_MSG:
                return decodeResponse(ben);

            case ERR_MSG:

                break;

            default:
                return null; //INVALID
        }

        return null;
    }

    private MessageBase decodeRequest(BencodeObject ben){
        MessageBase message;
        MessageBase.Method m = MessageBase.Method.valueOf(ben.getString("q"));

        switch(m){
            case PING:
                message = new PingRequest(tid);
                break;

            case FIND_NODE:
                message = new FindNodeRequest(tid);

                ((FindNodeRequest) message).setTarget(new UID(ben.getBencodeObject("a").getBytes("target")));

                //UNSURE IF THIS IS VALID OR ALLOWED
                /*
                if(ben.getBencodeObject("a").containsKey("wants")){
                    ((FindNodeRequest) message).setWantIPv4(ben.getBencodeObject("a").getBencodeArray("wants").contains("n4"));
                    ((FindNodeRequest) message).setWantIPv4(ben.getBencodeObject("a").getBencodeArray("wants").contains("n6"));
                }
                */

                break;

            case UNKNOWN:
            default:
                return null; //UNKNOWN
        }

        message.setUID(new UID(ben.getBencodeObject("a").getBytes("id")));

        return message;
    }

    private MessageBase decodeResponse(BencodeObject ben){
        MessageBase message;
        MessageBase.Method m = MessageBase.Method.valueOf(ben.getString("q"));

        switch(m){
            case PING:
                message = new PingResponse(tid);

            case FIND_NODE:
                message = new FindNodeResponse(tid);
                break;

            case UNKNOWN:
            default:
                return null; //UNKNOWN
        }

        //MAYBE DO THIS BETTER?
        message.setUID(new UID(ben.getBencodeObject("a").getBytes("id")));




        //msg.setUID();
        //msg.setDestination();
        //msg.setOrigin();;

        return message;
    }
}
