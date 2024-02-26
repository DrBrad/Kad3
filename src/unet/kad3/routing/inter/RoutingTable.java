package unet.kad3.routing.inter;

import unet.kad3.routing.kb.KBucket;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.net.InetAddress;

public abstract class RoutingTable {

    private UID uid;
    private KBucket[] kBuckets;

    private InetAddress consensusExternalAddress;

    public abstract void updatePublicIPConsensus(InetAddress source, InetAddress addr);

    public abstract InetAddress getConsensusExternalAddress();

    public abstract void insert(Node n);

    /*
    public synchronized List<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }
    */

    public abstract int getBucketId(UID k);
}
