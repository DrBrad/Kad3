package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeArray;
import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;

public class FindNodeRequest extends MessageBase {

    //ABSTRACT_LOOKUP_REQUEST
    private UID target;
    //private boolean ipv4 = true, ipv6 = true;

    public FindNodeRequest(){
        super(null, Method.FIND_NODE, Type.REQ_MSG);
    }

    public FindNodeRequest(byte[] tid){
        super(tid, Method.FIND_NODE, Type.REQ_MSG);
    }

    public UID getTarget(){
        return target;
    }

    public void setTarget(UID target){
        this.target = target;
    }

    protected void decode(BencodeObject ben){
        target = new UID(ben.getBytes("target"));

        //UNSURE IF THIS IS VALID OR ALLOWED
                /*
                if(ben.getBencodeObject("a").containsKey("wants")){
                    ((FindNodeRequest) message).setWantIPv4(ben.getBencodeObject("a").getBencodeArray("wants").contains("n4"));
                    ((FindNodeRequest) message).setWantIPv4(ben.getBencodeObject("a").getBencodeArray("wants").contains("n6"));
                }
                */
    }

    /*
    public boolean wantsIPv4(){
        return ipv4;
    }

    public void setWantIPv4(boolean ipv4){
        this.ipv4 = ipv4;
    }

    public boolean wantsIPv6(){
        return ipv6;
    }

    public void setWantIPv6(boolean ipv6){
        this.ipv6 = ipv6;
    }
    */

    @Override
    public BencodeObject getBencode(){
        //BencodeObject ben = super.getBencode();
        BencodeObject ben = super.getBencode();
        ben.getBencodeObject("a").put("target", target.getBytes());

        /*
        if(ipv4 || ipv6){
            BencodeArray w = new BencodeArray();

            if(ipv4){
                w.add("n4");
            }

            if(ipv6){
                w.add("n6");
            }

            ben.getBencodeObject("a").put("want", w);
        }
        */

        return ben;
    }
}
