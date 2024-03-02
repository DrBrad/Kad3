package unet.kad3.routing.inter;

import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class RoutingTable {

    protected UID uid;
    protected List<UIDChangeListener> listeners;

    public RoutingTable(){
        listeners = new ArrayList<>();
    }

    public abstract void updatePublicIPConsensus(InetAddress source, InetAddress addr);

    public abstract InetAddress getConsensusExternalAddress();

    public abstract void insert(Node n);

    public abstract void deriveUID();

    public UID getDerivedUID(){
        return uid;
    }

    public void addUIDChangeListener(UIDChangeListener listener){
        listeners.add(listener);
    }

    public boolean removeUIDChangeListener(UIDChangeListener listener){
        return listeners.remove(listener);
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

    public interface UIDChangeListener {

        void onChange(UID uid);
    }
}
