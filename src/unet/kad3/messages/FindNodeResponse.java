package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.Node;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static unet.kad3.utils.UID.ID_LENGTH;

public class FindNodeResponse extends MessageBase {

    /*
    * THIS CLASS COULD BE MADE MUCH BETTER BUT IT WORKS FOR NOW...
    * */

    public static final int IPV4_LENGTH = 4, IPV6_LENGTH = 16, NODE_CAP = 20;
    private List<Node> ipv4Nodes, ipv6Nodes;

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
        ipv4Nodes = new ArrayList<>();
        ipv6Nodes = new ArrayList<>();
    }

    protected void decode(BencodeObject ben){
        if(ben.containsKey("nodes")){
            byte[] buf = ben.getBencodeObject("r").getBytes("nodes");
            addNodes(buf, Types.IPv4);
        }

        if(ben.containsKey("nodes6")){
            byte[] buf = ben.getBencodeObject("r").getBytes("nodes6");
            addNodes(buf, Types.IPv6);
        }
    }

    //BASE FOR IPv4 vs IPv6
    private void addNodes(byte[] buf, Types type){
        List<Node> nodes;

        switch(type){
            case IPv4:
                nodes = ipv4Nodes;
                break;

            case IPv6:
                nodes = ipv6Nodes;
                break;

            default:
                return;
        }

        byte[] bid = new byte[ID_LENGTH];
        byte[] addr = new byte[type.getAddressLength()];
        int position = 0;
        int port;

        while(position < buf.length){
            System.arraycopy(buf, position, bid, 0, bid.length);
            position += bid.length;

            System.arraycopy(buf, position, addr, 0, addr.length);
            position += addr.length;

            port = ((buf[position] & 0xff) << 8) | (buf[position+1] & 0xff);
            position += 2;

            try{
                nodes.add(new Node(bid, InetAddress.getByAddress(addr), port));

            }catch(UnknownHostException e){
                e.printStackTrace();
            }
        }
    }

    private byte[] encodeNodes(Types type){
        List<Node> nodes;

        switch(type){
            case IPv4:
                nodes = ipv4Nodes;
                break;

            case IPv6:
                nodes = ipv6Nodes;
                break;

            default:
                return null;
        }

        byte[] buf = new byte[nodes.size()*ID_LENGTH*IPV6_LENGTH*2];
        int position = 0;

        for(Node n : nodes){
            byte[] bid = n.getUID().getBytes();
            System.arraycopy(bid, 0, buf, position, bid.length);
            position += bid.length;

            byte[] addr = n.getAddress().getAddress();
            System.arraycopy(addr, 0, buf, position, addr.length);
            position += addr.length;

            //PORT TIME...
            buf[position] = (byte) ((n.getPort() >> 8) & 0xff);
            buf[position+1] = (byte) (n.getPort() & 0xff);
            position += 2;
        }

        return buf;
    }

    public void addNode(Node node){
        if(node.getAddress() instanceof Inet4Address){
            if(ipv4Nodes.size() > NODE_CAP){
                throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
            }

            ipv4Nodes.add(node);
            return;
        }

        if(ipv6Nodes.size() > NODE_CAP){
            throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
        }

        ipv6Nodes.add(node);
    }

    public boolean containsNode(Node node){
        if(node.getAddress() instanceof Inet4Address){
            return ipv4Nodes.contains(node);
        }
        return ipv6Nodes.contains(node);
    }

    public boolean removeNode(Node node){
        if(node.getAddress() instanceof Inet4Address){
            return ipv4Nodes.remove(node);
        }
        return ipv6Nodes.remove(node);
    }

    public List<Node> getIPv4Nodes(){
        return ipv4Nodes;
    }

    public List<Node> getIPv6Nodes(){
        return ipv6Nodes;
    }

    public List<Node> getAllNodes(){
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(ipv4Nodes);
        nodes.addAll(ipv6Nodes);
        return nodes;
    }

    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = super.getBencode();

        if(!ipv4Nodes.isEmpty()){
            ben.getBencodeObject("r").put("nodes", encodeNodes(Types.IPv4));
        }

        if(!ipv6Nodes.isEmpty()){
            ben.getBencodeObject("r").put("nodes6", encodeNodes(Types.IPv6));
        }

        return ben;
    }

    public enum Types {

        IPv4 {
            int getAddressLength(){
                return IPV4_LENGTH;
            }
        },
        IPv6 {
            int getAddressLength(){
                return IPV6_LENGTH;
            }
        };

        int getAddressLength(){
            return 0;
        }
    }
}
