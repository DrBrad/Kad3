package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class FindNodeResponse extends MessageBase {

    //ABSTRACT_LOOKUP_RESPONSE
    public static final int IPV4_LENGTH = 4, IPV6_LENGTH = 16;
    private List<Node> nodes;

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
        nodes = new ArrayList<>();
    }

    protected void decode(BencodeObject ben){
        if(ben.containsKey("nodes")){
            byte[] buf = ben.getBencodeObject("r").getBytes("nodes");
            addNodes(buf, IPV4_LENGTH);
        }

        if(ben.containsKey("nodes6")){
            byte[] buf = ben.getBencodeObject("r").getBytes("nodes6");
            addNodes(buf, IPV6_LENGTH);
        }
    }

    private void addNodes(byte[] buf, int addressLength){
        byte[] bid = new byte[20];
        byte[] addr = new byte[addressLength];
        int position = 0;
        int port;

        while(position < buf.length){
            System.arraycopy(buf, position, bid, 0, bid.length);
            position += bid.length;

            System.arraycopy(buf, position, addr, 0, addr.length);
            position += addr.length;

            port = ((buf[position] & 0xff) << 8) | (buf[position + 1] & 0xff);
            position += 2;

            try{
                nodes.add(new Node(bid, InetAddress.getByAddress(addr), port));
            }catch(UnknownHostException e){
                e.printStackTrace();
            }
        }
    }

    public void addNode(Node node){
        nodes.add(node);
    }

    public boolean containsNode(Node node){
        return nodes.contains(node);
    }

    public void removeNode(Node node){
        nodes.remove(node);
    }

    public List<Node> getNodes(){
        return nodes;
    }

    @Override
    public BencodeObject getBencode(){
        return super.getBencode();
    }
}
