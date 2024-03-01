package unet.kad3.routing.inter;

import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.net.InetAddress;
import java.util.List;

public abstract class RoutingTable {

    protected UID uid;

    public abstract void updatePublicIPConsensus(InetAddress source, InetAddress addr);

    public abstract InetAddress getConsensusExternalAddress();

    public abstract void insert(Node n);

    public abstract void deriveUID();

    public UID getDerivedUID(){
        return uid;
    }

    /*
    public synchronized List<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }
    */

    public abstract int getBucketUID(UID k);

    public abstract List<Node> getAllNodes();

    public abstract List<Node> findClosest(UID k, int r);

    public abstract int getBucketSize(int i);

    public abstract List<Node> getAllUnqueriedNodes();

}
