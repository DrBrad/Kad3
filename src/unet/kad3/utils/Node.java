package unet.kad3.utils;

import unet.kad3.libs.CRC32C;

import java.net.InetAddress;
import java.util.Random;

public class Node {

    public static final byte[] V4_MASK = { 0x03, 0x0f, 0x3f, (byte) 0xff };
    public static final byte[] V6_MASK = { 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, (byte) 0xff };
    public static long QUERY_TIME = 3600000;

    protected UID uid;
    protected InetAddress address;
    protected int port;

    private int stale;
    private long lastSeen;

    //FOR TESTING PURPOSES ONLY...
    /*
    public Node(InetAddress address, int port){
        this.address = address;
        this.port = port;

        //THIS WILL LIKELY BE MOVED STRAIT THE FUCK TO THE ROUTING TABLE AS WE DON'T MAKE ID's FOR OTHERS...

        //DERIVE THE KEY...
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
    */

    //CREATE NODE BY HEX AS WELL...

    public Node(String uid, InetAddress address, int port){
        this(new UID(uid), address, port);
    }

    public Node(byte[] bid, InetAddress address, int port){
        this(new UID(bid), address, port);
    }

    public Node(UID uid, InetAddress address, int port){
        this.uid = uid;
        this.address = address;
        this.port = port;
    }

    public boolean hasSecureID(){
        byte[] ip = address.getAddress();
        byte[] mask = ip.length == 4 ? V4_MASK : V6_MASK;

        for(int i = 0; i < mask.length; i++){
            ip[i] &= mask[i];
        }

        int r = uid.bid[19] & 0x7;

        ip[0] |= r << 5;

        CRC32C c = new CRC32C();
        c.update(ip, 0, Math.min(ip.length, 8));
        int crc = (int) c.getValue();

        return (((Byte.toUnsignedInt(uid.bid[0]) << 24 |
                Byte.toUnsignedInt(uid.bid[1]) << 16 |
                Byte.toUnsignedInt(uid.bid[2]) << 8 |
                Byte.toUnsignedInt(uid.bid[3])) ^ crc) & 0xff_ff_f8_00) == 0;
        //return ((uid.getInt(0) ^ crc) & 0xff_ff_f8_00) == 0;
    }

    public UID getUID(){
        return uid;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }



    //DETAILS
    public void setSeen(){
        stale = 0;
        lastSeen = System.currentTimeMillis();
    }

    public void markStale(){
        stale++;
    }

    public int getStale(){
        return stale;
    }

    public long getLastSeen(){
        return lastSeen;
    }

    public boolean hasQueried(long now){
        return (lastSeen+QUERY_TIME < now);
    }

    public boolean verify(Object o){
        if(o instanceof Node){
            return uid.equals(((Node) o).uid);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return address.hashCode()+port;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            return hashCode() == o.hashCode();
            //return address.equals(((Node) o).address) && port == ((Node) o).port;
        }
        return false;
    }

    @Override
    public String toString(){
        return "{ \033[0;34mUID\033[0m: \033[0;35m"+uid.toString()+"\033[0m, \033[0;34mADDRESS\033[0m: \033[0;35m"+address.getHostAddress()+"\033[0m, \033[0;34mPORT\033[0m: \033[0;35m"+port+"\033[0m }";
    }
}
