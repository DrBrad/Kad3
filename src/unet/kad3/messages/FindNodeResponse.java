package unet.kad3.messages;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.Node;
import unet.kad3.utils.net.AddressType;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import static unet.kad3.utils.NodeUtils.*;

public class FindNodeResponse extends MessageBase {

    /*
    * THIS CLASS COULD BE MADE MUCH BETTER BUT IT WORKS FOR NOW...
    * */

    public static final int NODE_CAP = 20;
    private List<Node> ipv4Nodes, ipv6Nodes;

    public FindNodeResponse(byte[] tid){
        super(tid, Method.FIND_NODE, Type.RSP_MSG);
        ipv4Nodes = new ArrayList<>();
        ipv6Nodes = new ArrayList<>();
    }

    public void addNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
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

    public void addNodes(List<Node> nodes){
        for(Node n : nodes){
            if(n.getHostAddress() instanceof Inet4Address){
                if(ipv4Nodes.size() < NODE_CAP){
                    ipv4Nodes.add(n);
                }
                continue;
            }

            if(ipv6Nodes.size() < NODE_CAP){
                ipv6Nodes.add(n);
            }
        }
    }

    public void addNodes(List<Node> nodes, AddressType type){
        switch(type){
            case IPv4:
                ipv4Nodes.addAll(nodes);
                break;

            case IPv6:
                ipv6Nodes.addAll(nodes);
                break;
        }
    }

    public boolean containsNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            return ipv4Nodes.contains(node);
        }
        return ipv6Nodes.contains(node);
    }

    public boolean removeNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
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
            ben.getBencodeObject(t.innerKey()).put("nodes", packNodes(ipv4Nodes, AddressType.IPv4));
        }

        if(!ipv6Nodes.isEmpty()){
            ben.getBencodeObject(t.innerKey()).put("nodes6", packNodes(ipv6Nodes, AddressType.IPv6));
        }

        return ben;
    }
}
