package unet.kad3.routing.kb;

import unet.kad3.libs.CRC32C;
import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.FindNodeResponse;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.PingResponse;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.net.InetAddress;
import java.util.*;

import static unet.kad3.utils.Node.*;

public class KRoutingTable extends RoutingTable {

    private UID uid;
    private KBucket[] kBuckets;
    private InetAddress consensusExternalAddress;

    private LinkedHashMap<InetAddress, InetAddress> originPairs  = new LinkedHashMap<>(64, 0.75f, true){
        @Override
        protected boolean removeEldestEntry(Map.Entry<InetAddress, InetAddress> eldest){
            return (size() > 64);
        }
    };

    public KRoutingTable(){
        //uid = new UID(new byte[]{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
        //use UPnP to attempt getting IP
        //If fails go to fall back method...
        //We need to somehow allow the refresh to derive our ID...

        kBuckets = new KBucket[UID.ID_LENGTH];
        for(int i = 0; i < UID.ID_LENGTH; i++){
            kBuckets[i] = new KBucket();
        }
    }

    @Override
    public void updatePublicIPConsensus(InetAddress source, InetAddress addr){
        //if(!AddressUtils.isGlobalUnicast(addr.getAddress()))
        //    return;

        synchronized(originPairs){
            originPairs.put(source, addr);
            if(originPairs.size() > 20 && !addr.equals(consensusExternalAddress)){
                List<InetAddress> k = new ArrayList<>(originPairs.values());
                short res = 0, count = 1;

                for(short i = 1; i < k.size(); i++){
                    count += (k.get(i) == k.get(res)) ? 1 : -1;

                    if(count == 0){
                        res = i;
                        count = 1;
                    }
                }

                consensusExternalAddress = k.get(res);
            }
        }
    }

    @Override
    public InetAddress getConsensusExternalAddress(){
        return consensusExternalAddress;
    }

    @Override
    public void deriveUID(InetAddress address){
        byte[] ip = address.getAddress();
        byte[] mask = ip.length == 4 ? V4_MASK : V6_MASK;

        for(int i = 0; i < ip.length; i++){
            ip[i] &= mask[i];
        }

        Random random = new Random();
        int rand = random.nextInt() & 0xFF;
        int r = rand & 0x7;

        ip[0] |= r << 5;

        CRC32C c = new CRC32C();
        c.update(ip, 0, ip.length);
        int crc = (int) c.getValue();

        // idk about this stuff below
        byte[] bid = new byte[UID.ID_LENGTH];
        bid[0] = (byte) ((crc >> 24) & 0xFF);
        bid[1] = (byte) ((crc >> 16) & 0xFF);
        bid[2] = (byte) (((crc >> 8) & 0xF8) | (random.nextInt() & 0x7));

        for(int i = 3; i < 19; i++){
            bid[i] = (byte) (random.nextInt() & 0xFF);
        }

        bid[19] = (byte) (rand & 0xFF);
        uid = new UID(bid);
    }

    @Override
    public synchronized void insert(Node n){
        if(n.hasSecureID() && !uid.equals(n.getUID())){
            int id = getBucketUID(n.getUID());
            //boolean containsIP = getAllNodes().contains(n);

            boolean containsIP = false;
            for(KBucket b : kBuckets){
                if(b.containsIP(n)){
                    containsIP = true;
                    break;
                }
            }

            boolean containsUID = kBuckets[id].containsUID(n);

            //START WITH N BUCKET THEN THROUGH THE REST - DONT ADD TO LIST THEN LOOP - NO LIST...

            if(containsIP == containsUID){
                kBuckets[id].insert(n);
            }
            //EXACT MATCH ADD

            //ETHER MATCH IGNORE

            //NO MATCH ADD
        }


        //WE NEED TO INCLUDE CACHE


        /*
        if(n.hasSecureID()){
            if(!uid.equals(n.getUID())){
                final int id = getBucketId(uid);
                final boolean uidExists = kBuckets[id].containsUID(n),
                        ipExists = Arrays.stream(kBuckets).anyMatch(k -> k.containsIP(n));

                /.*
                boolean ipExists = false;

                for(KBucket b : kBuckets){
                    if(b.containsIP(n)){
                        ipExists = true;
                        break;
                    }
                }
                *./

                if((ipExists && uidExists) || (!ipExists && !uidExists)){
                    //ADD
                }else{
                    //UID COULD BE FINE AND IP CHANGED - IF THATS THE CASE UID IS INVALID - CANT REACH THIS POINT...
                    //UID COULD BE FINE AND PORT CHANGED - IF THATS THE CASE MAYBE IGNORE NOT SURE...
                    //UID CHANGED IP AND PORT FINE - IF THATS THE CASE THE NODE MAY BE RESTARTING OR CONSTANTLY CHANGING
                    //                              DECIDE ON WHAT TO DO...

                    //POSSIBLY IGNORE
                }


            }else{
                //THIS IS US... SHOULDN'T HAPPEN
            }
        }else{
            //INVALID ID
        }

        //CHECK IF ITS NOT ME... - KIND OF STUPID BUT NECISSARY
            //CHECK IF UID IS VALID
                //CHECK IF ARRAY CONTAINS IP+PORT && !UID MATCH
                //CHECK OPPOSING
                //ELSE
        */
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

    @Override
    public synchronized int getBucketUID(UID k){
        int bid = uid.getDistance(k)-1;
        return bid < 0 ? 0 : bid;
    }



    //PROBABLY MOVE THESE CALL FUNCTIONS OUT OF ROUTING TABLE...
    public void ping(PingRequest request){
        /*
        if (!isRunning()) {
            return;
        }

        // ignore requests we get from ourself
        if (node.isLocalId(r.getID())) {
            return;
        }
        */

        PingResponse response = new PingResponse(request.getTransactionID());
        response.setDestination(request.getOriginIP(), request.getOriginPort());
        //request.getServer().sendMessage(response);

        //node.recieved(r);
    }

    public void findNode(FindNodeRequest request){
        /*
        if (!isRunning()) {
            return;
        }

        // ignore requests we get from ourself
        if (node.isLocalId(r.getID())) {
            return;
        }

        AbstractLookupResponse response;
        if(r instanceof FindNodeRequest)
            response = new FindNodeResponse(r.getMTID());
        else
            response = new UnknownTypeResponse(r.getMTID());

        populateResponse(r.getTarget(), response, r.doesWant4() ? DHTConstants.MAX_ENTRIES_PER_BUCKET : 0, r.doesWant6() ? DHTConstants.MAX_ENTRIES_PER_BUCKET : 0);

        response.setDestination(r.getOrigin());
        r.getServer().sendMessage(response);

        node.recieved(r);
        */

        FindNodeResponse response = new FindNodeResponse(request.getTransactionID());

        response.setDestination(request.getOriginIP(), request.getOriginPort());
    }






    @Override
    public String toString(){
        int s = 0, c = 0;
        for(KBucket b : kBuckets){
            s += b.size();
            c += b.csize();
        }
        return s+" - SIZE    "+c+"  - CACHE";
    }

    /*
    private LocalNode local;
    private KBucket[] kBuckets;

    public RoutingTable(int port){
        local = (LocalNode) Node.getLocalNode(port);

        kBuckets = new KBucket[UID.ID_LENGTH];
        for(int i = 0; i < UID.ID_LENGTH; i++){
            kBuckets[i] = new KBucket();
        }
    }

    public LocalNode getLocal(){
        return local;
    }

    public synchronized void add(Contact c){
        if(!c.equals(local)){
            for(KBucket kBucket : kBuckets){
                if(kBucket.contains(c) && !kBucket.verify(c)){
                    kBucket.removeSpoof(c);
                    return;
                }
            }

            kBuckets[getBucketId(c.getNode().getUID())].add(c);
        }
    }

    public synchronized void add(Node n){
        add(new Contact(n));
    }

    public synchronized int getBucketId(UID k){
        int bid = local.getUID().getDistance(k)-1;
        return bid < 0 ? 0 : bid;
    }

    public synchronized List<Contact> getAllNodes(){
        ArrayList<Contact> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getContacts());
        }
        return nodes;
    }

    public synchronized List<Contact> findClosest(){
        return null;
    }
*/
    /*
        public synchronized List<Node> findClosest(UID k, int r){
        TreeSet<Node> sortedSet = new TreeSet<>(new KComparator(k));
        sortedSet.addAll(getAllNodes());

        List<Node> closest = new ArrayList<>(r);

        int count = 0;
        for(Node n : sortedSet){
            closest.add(n);
            if(count++ == r){
                break;
            }
        }
        return closest;
    }

    public synchronized List<Node> findClosestWithLocal(UID k, int r){
        TreeSet<Node> sortedSet = new TreeSet<>(new KComparator(k));
        sortedSet.addAll(getAllNodes());
        sortedSet.add(local.getNode());

        List<Node> closest = new ArrayList<>(r);

        int count = 0;
        for(Node n : sortedSet){
            closest.add(n);
            if(count++ == r){
                break;
            }
        }
        return closest;
    }

    public synchronized int getBucketSize(int i){
        return kbuckets[i].size();
    }

    public synchronized int getBucketId(UID k){
        int bid = local.getNode().getUID().getDistance(k)-1;
        return bid < 0 ? 0 : bid;
    }

    */
}
