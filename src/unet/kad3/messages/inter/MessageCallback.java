package unet.kad3.messages.inter;

import unet.kad3.messages.ErrorMessage;

public abstract class MessageCallback {

    public abstract void onResponse(MessageBase message);

    public void onErrorResponse(ErrorMessage message){
    }

    //public void onException(MessageException exception){
    //}

    public void onStalled(){
    }
}
