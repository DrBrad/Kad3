package unet.kad3.messages;

public class FindNodeRequest extends MessageBase {

    //ABSTRACT_LOOKUP_REQUEST

    public FindNodeRequest(){
        super(null, Method.FIND_NODE, Type.RSP_MSG);
    }
}
