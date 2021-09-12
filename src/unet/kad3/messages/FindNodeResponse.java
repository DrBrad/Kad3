package unet.kad3.messages;

public class FindNodeResponse extends MessageBase {

    //ABSTRACT_LOOKUP_RESPONSE

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
    }
}
