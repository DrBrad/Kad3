package unet.kad3.messages.inter;

public interface MessageCallback {

    void onResponse(MessageBase request, MessageBase response);
}
