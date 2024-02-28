package unet.kad3;

import unet.kad3.messages.FindNodeRequest;
import unet.kad3.messages.MessageDecoder;
import unet.kad3.messages.PingRequest;
import unet.kad3.messages.inter.MessageBase;
import unet.kad3.utils.UID;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static unet.kad3.kad.RPCServer.TID_LENGTH;

public class MessageTest {

    public static void main(String[] args){
        UID uid = new UID("992c105ffed716245654fd4c2c4d71e0f2df58cc");
        pingRequest(uid);
        findNodeRequest(uid);
    }

    public static void pingRequest(UID uid){
        PingRequest r = new PingRequest(new byte[TID_LENGTH]);
        r.setUID(uid);
        System.out.println("PING MESSAGE:");
        System.out.println(r);

        byte[] b = r.encode();
        MessageBase m = new MessageDecoder(b).parse();
        System.out.println("Encoding > Decode Match: "+matches(b, m.encode()));
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public static void findNodeRequest(UID uid){
        FindNodeRequest r = new FindNodeRequest(new byte[TID_LENGTH]);
        r.setUID(uid);
        r.setTarget(new UID("5a3ce9c14e7a08645677bbd1cfe7d8f956d53256"));
        System.out.println("FIND_NODE MESSAGE:");
        System.out.println(r);

        byte[] b = r.encode();
        MessageBase m = new MessageDecoder(b).parse();
        System.out.println("Encoding > Decode Match: "+matches(b, m.encode()));
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public static boolean matches(byte[] a, byte[] b){
        return Arrays.equals(digestBytes("SHA1", a), digestBytes("SHA1", b));
    }

    private static byte[] digestBytes(String algorithm, byte[] input){
        try{
            MessageDigest m = MessageDigest.getInstance(algorithm);
            m.update(input, 0, input.length);
            return m.digest();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }
}
