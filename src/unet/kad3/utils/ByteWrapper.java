package unet.kad3.utils;

import java.util.Arrays;

public class ByteWrapper {

    public final byte[] b;
    private final int h;

    public ByteWrapper(byte[] b){
        this.b = b;
        h = Arrays.hashCode(b);
    }

    @Override
    public int hashCode(){
        return h;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof ByteWrapper && Arrays.equals(b, ((ByteWrapper)obj).b);
    }
}
