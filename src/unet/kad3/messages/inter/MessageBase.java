package unet.kad3.messages.inter;

import unet.kad3.kad.calls.RPCResponseCall;
import unet.kad3.kad.RPCServer;
import unet.kad3.libs.bencode.variables.BencodeObject;
import unet.kad3.utils.net.AddressUtils;
import unet.kad3.utils.UID;

import java.net.InetAddress;

import static unet.kad3.Build.*;

public class MessageBase {

    protected UID uid;
    protected Method m;
    protected Type t;

    protected byte[] tid;

    protected InetAddress destinationIP, originIP, publicIP; //LOTS OF QUESTIONS WITH THIS...
    protected int destinationPort, originPort;

    protected RPCResponseCall associatedCall;
    private RPCServer server;



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
        BencodeObject ben = getBencode();
        ben.put("t", tid); //TRANSACTION ID
        ben.put("v", VERSION_CODE); //VERSION

        ben.put(Type.TYPE_KEY, getType().getRPCTypeName());

        switch(t){
            case REQ_MSG:
                ben.put(t.getRPCTypeName(), m.getRPCName());
                break;

            case RSP_MSG:
                if(destinationIP != null){
                    ben.put("ip", AddressUtils.packAddress(publicIP, originPort)); //PACK MY IP ADDRESS
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


    public void setDestination(InetAddress address, int port){
        this.destinationIP = address;
        this.destinationPort = port;
    }

    public InetAddress getDestinationIP(){
        return destinationIP;
    }

    public int getDestinationPort(){
        return destinationPort;
    }



    public void setOrigin(InetAddress address, int port){
        this.publicIP = address;
        this.originPort = port;
    }

    public InetAddress getOriginIP(){
        return originIP;
    }

    public int getOriginPort(){
        return originPort;
    }

    public void setPublicIP(InetAddress address){
        this.publicIP = address;
    }

    public InetAddress getPublicIP(){
        return publicIP;
    }



    public Method getMethod(){
        return m;
    }

    public Type getType(){
        return t;
    }

    public void setAssociatedCall(RPCResponseCall associatedCall){
        this.associatedCall = associatedCall;
    }

    public RPCResponseCall getAssociatedCall(){
        return associatedCall;
    }

    public void setServer(RPCServer server){
        this.server = server;
    }

    public RPCServer getServer(){
        return server;
    }

    //GET / SET RPCServer...?

    //RENAME THIS SHIT AS WELL
    public byte[] encode(){
        return getBencode().encode();
    }

    public static enum Method {

        PING,
        FIND_NODE,
        GET_PEERS,
        ANNOUNCE_PEER,
        GET,
        PUT,
        SAMPLE_INFOHASHES,
        UNKNOWN;

        String getRPCName(){
            return name().toLowerCase();
        }
    }

    public static enum Type {

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

        public static final String TYPE_KEY = "y";
    }

    @Override
    public String toString(){
        return null;
    }
}
