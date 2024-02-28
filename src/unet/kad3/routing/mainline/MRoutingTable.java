package unet.kad3.routing.mainline;

import unet.kad3.libs.CRC32C;
import unet.kad3.routing.inter.RoutingTable;
import unet.kad3.utils.Node;
import unet.kad3.utils.UID;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import static unet.kad3.utils.Node.*;

public class MRoutingTable extends RoutingTable {

    //TODO

    private ArrayList<MBucket> buckets = new ArrayList<>();
    private InetAddress consensusExternalAddress;

    public MRoutingTable(){
        try{
            consensusExternalAddress = Inet4Address.getLocalHost();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }

        deriveUID();

        buckets.add(new MBucket()); //CLOSEST
        buckets.add(new MBucket()); //FURTHEST
    }

    @Override
    public void updatePublicIPConsensus(InetAddress source, InetAddress addr){
    }

    @Override
    public InetAddress getConsensusExternalAddress(){
        return null;
    }

    @Override
    public void deriveUID(){
        byte[] ip = consensusExternalAddress.getAddress();
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
        if(n.hasSecureID()){ //NODE VERIFICATION CHECK
            if(!uid.equals(n.getUID())){ //SELF CHECK
                int b = getBucketUID(n.getUID());

                //ARE WE EVEN CHECKING FOR IP/PORT MATCHES...

                if(b < buckets.size()){
                    if(!buckets.get(b).isFull()){
                        buckets.get(b).insert(n);
                    }//ELSE...???? - I GUESS THIS IS FINE BECAUSE THIS SHOULD ALREADY BE FULL AFTER SPLIT...

                }else{

                    //LOOK INTO THIS....
                    //NOT SURE IF THIS IS DONE VERY WELL...
                    if(buckets.get(buckets.size()-1).isFull()){
                        buckets.add(buckets.size(), new MBucket());

                        //SPLIT TIME
                        for(int i = buckets.get(buckets.size()-2).size()-1; i > -1; i--){
                            //for(Node ns : buckets.get(buckets.size()-2).list()){
                            Node ns = buckets.get(buckets.size()-2).get(i);
                            int j = getBucketUID(ns.getUID());
                            if(j == buckets.size()-1){
                                buckets.get(buckets.size()-2).remove(ns);
                                buckets.get(buckets.size()-1).insert(ns);
                            }
                        }
                        System.out.println("SPLIT ("+(buckets.size()-2)+" - "+(buckets.size()-1)+")");
                    }

                    if(!buckets.get(buckets.size()-1).isFull()){
                        buckets.get(buckets.size()-1).insert(n);
                    }

                }


            }
        }
    }

    @Override
    public synchronized int getBucketUID(UID k){
        return uid.getDistance(k);//-1;
    }

    /*
    public synchronized List<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        for(KBucket kBucket : kBuckets){
            nodes.addAll(kBucket.getAllNodes());
        }
        return nodes;
    }

    public synchronized int getBucketId(UID k){
        int bid = uid.getDistance(k)-1;
        return bid < 0 ? 0 : bid;
    }
    */
}
