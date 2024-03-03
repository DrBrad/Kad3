package unet.kad3.messages.inter;

public abstract class MessageCallback {

    public abstract void onResponse(MessageBase message);

    public void onStalled(){
    }
}
