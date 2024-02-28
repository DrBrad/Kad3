package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeArray;
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

    public static final int IPV4_LENGTH = 4, IPV6_LENGTH = 16, NODE_CAP = 20;
    private List<Node> ipv4Nodes, ipv6Nodes;

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
        ipv4Nodes = new ArrayList<>();
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

    //BASE FOR IPv4 vs IPv6
    private void addNodes(byte[] buf, int addressLength){
        byte[] bid = new byte[ID_LENGTH];
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

        //CAN WE JUST USE A FUNCTION TO CALL THIS CODE 1 TIME FOR BOTH ARRAYS...
        if(!ipv4Nodes.isEmpty()){
            byte[] a = new byte[ipv4Nodes.size()*ID_LENGTH*IPV4_LENGTH*2];
            int position = 0;

            for(Node n : ipv4Nodes){
                byte[] bid = n.getUID().getBytes();
                System.arraycopy(bid, 0, a, position, bid.length);
                position += bid.length;

                byte[] addr = n.getAddress().getAddress();
                System.arraycopy(addr, 0, a, position, addr.length);
                position += addr.length;

                //PORT TIME...
                a[position] = (byte) ((n.getPort() >> 8) & 0xff);
                a[position+1] = (byte) (n.getPort() & 0xff);
                position += 2;
            }

            ben.getBencodeObject("r").put("nodes", a);
        }

        if(!ipv6Nodes.isEmpty()){
            byte[] a = new byte[ipv6Nodes.size()*ID_LENGTH*IPV6_LENGTH*2];
            int position = 0;

            for(Node n : ipv6Nodes){
                byte[] bid = n.getUID().getBytes();
                System.arraycopy(bid, 0, a, position, bid.length);
                position += bid.length;

                byte[] addr = n.getAddress().getAddress();
                System.arraycopy(addr, 0, a, position, addr.length);
                position += addr.length;

                //PORT TIME...
                a[position] = (byte) ((n.getPort() >> 8) & 0xff);
                a[position+1] = (byte) (n.getPort() & 0xff);
                position += 2;
            }

            ben.getBencodeObject("r").put("nodes6", a);
        }

        return ben;
    }
}
