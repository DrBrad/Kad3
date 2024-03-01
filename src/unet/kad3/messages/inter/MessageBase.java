package unet.kad3.messages.inter;

import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.utils.net.AddressUtils;
import unet.kad3.utils.UID;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static unet.kad3.Build.*;

public class MessageBase {

    protected UID uid;
    protected Method m;
    protected Type t;

    protected byte[] tid;

    protected InetSocketAddress destination, origin, publicAddress;

    //TRANSACTION ID
    //VERSION... "DONT HARD CODE THIS SHIT..."
    //WHERE TF ARE WE GOING TO SEND THE ID...
    //      THE RPC SERVER WILL SET THE TID AND THE UID...

    public MessageBase(byte[] tid, Method m, Type t){
        this.tid = tid;
        this.m = m;
        this.t = t;
    }

    public BencodeObject getBencode(){
        BencodeObject ben = new BencodeObject();
        ben.put("t", tid); //TRANSACTION ID
        ben.put("v", VERSION_CODE); //VERSION

        ben.put(Type.TYPE_KEY, getType().getRPCTypeName());

        switch(t){
            case REQ_MSG:
                ben.put(t.getRPCTypeName(), m.getRPCName());
                ben.put("a", new BencodeObject());
                ben.getBencodeObject("a").put("id", uid.getBytes());
                break;

            case RSP_MSG:
                ben.put("r", new BencodeObject());
                ben.getBencodeObject("r").put("id", uid.getBytes());

                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;
        }

        return ben;
    }

    public void setUID(UID uid){
        this.uid = uid;
    }

    public UID getUID(){
        return uid;
    }

    public void setTransactionID(byte[] tid){
        this.tid = tid;
    }

    public byte[] getTransactionID(){
        return tid;
    }

    public InetSocketAddress getPublic(){
        return publicAddress;
    }

    public InetAddress getPublicAddress(){
        return publicAddress.getAddress();
    }

    public int getPublicPort(){
        return publicAddress.getPort();
    }

    public void setPublic(InetAddress address, int port){
        publicAddress = new InetSocketAddress(address, port);
    }

    public void setPublic(InetSocketAddress publicAddress){
        this.publicAddress = publicAddress;
    }

    public InetSocketAddress getDestination(){
        return destination;
    }

    public InetAddress getDestinationAddress(){
        return destination.getAddress();
    }

    public int getDestinationPort(){
        return destination.getPort();
    }

    public void setDestination(InetAddress address, int port){
        destination = new InetSocketAddress(address, port);
    }

    public void setDestination(InetSocketAddress destination){
        this.destination = destination;
    }

    public InetSocketAddress getOrigin(){
        return origin;
    }

    public InetAddress getOriginAddress(){
        return origin.getAddress();
    }

    public int getOriginPort(){
        return origin.getPort();
    }

    public void setOrigin(InetAddress address, int port){
        origin = new InetSocketAddress(address, port);
    }

    public void setOrigin(InetSocketAddress origin){
        this.origin = origin;
    }

    public Method getMethod(){
        return m;
    }

    public Type getType(){
        return t;
    }

    //GET / SET RPCServer...?

    //RENAME THIS SHIT AS WELL
    public byte[] encode(){
        return getBencode().encode();
    }

    public enum Method {

        PING,
        FIND_NODE,
        GET_PEERS,
        ANNOUNCE_PEER,
        GET,
        PUT,
        SAMPLE_INFOHASHES,
        UNKNOWN;

        public static Method fromRPCName(String key){
            return Method.valueOf(key.toUpperCase());
        }

        String getRPCName(){
            return name().toLowerCase();
        }
    }

    public enum Type {

        REQ_MSG {
            @Override
            String innerKey(){
                return "a";
            }
            @Override
            String getRPCTypeName(){
                return "q";
            }
        }, RSP_MSG {
            @Override
            String innerKey(){
                return "r";
            }
            @Override
            String getRPCTypeName(){
                return "r";
            }
        }, ERR_MSG {
            @Override
            String innerKey(){
                return "e";
            }
            @Override
            String getRPCTypeName(){
                return "e";
            }
        }, INVALID;

        String innerKey(){
            return null;
        }

        String getRPCTypeName(){
            return name().toLowerCase();
            //return null;
        }

        public static Type fromRPCTypeName(String key){
            key = key.toLowerCase();

            for(Type t : Type.values()){
                if(key.equals(t.getRPCTypeName())){
                    return t;
                }
            }

            throw new IllegalArgumentException("Inner key not found.");
        }

        public static final String TYPE_KEY = "y";
    }

    @Override
    public String toString(){
        return getBencode().toString();
    }
}
