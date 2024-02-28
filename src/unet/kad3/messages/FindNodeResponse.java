package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.Node;

import java.util.ArrayList;
import java.util.List;

public class FindNodeResponse extends MessageBase {

    //ABSTRACT_LOOKUP_RESPONSE
    private List<Node> nodes;

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
        nodes = new ArrayList<>();
    }

    protected void decode(BencodeObject ben){
        if(ben.containsKey("nodes")){

        }

        if(ben.containsKey("nodes6")){

        }

        /*
        ByteBuffer buf;
        byte[] bid = new byte[20];
        byte[] addr;
        int port;

        if(ben.getBencodeObject("r").containsKey("nodes")){
            buf = ByteBuffer.wrap(ben.getBencodeObject("r").getBytes("nodes"));
            addr = new byte[4];

        }else if(ben.getBencodeObject("r").containsKey("nodes6")){
            buf = ByteBuffer.wrap(ben.getBencodeObject("r").getBytes("nodes6"));
            addr = new byte[16];

        }else{
            throw new NullPointerException("No nodes received");
        }

        while(buf.position() != buf.limit()){
            buf.get(bid);
            buf.get(addr);
            port = ((buf.get() & 0xff) << 8) | (buf.get() & 0xff);

            Bep42 peer = new Bep42(InetAddress.getByAddress(addr), port, bid);
            System.out.println("PEER:     "+peer);
        }
        */

    }

    public List<Node> getNodes(){
        return null;
    }

    @Override
    public BencodeObject getBencode(){
        return super.getBencode();
    }
}
