package com.sharesmile.share.network;

import com.sharesmile.share.core.UnObfuscable;

import java.util.List;

/**
 Created by ankitmaheshwari1 on 22/12/15.
 */
public class ServerErrorResponse implements UnObfuscable{

    private static final String TAG = "ServerErrorResponse";

    private Object message;

    public String getMessage() {
        if (message == null) {
            return "Couldn't fetch message from responseObject";
        }
        if (message instanceof String) {
            return message.toString();
        } else if (message instanceof List) {
            List<String> messageList = (List<String>) message;
            StringBuilder sb = new StringBuilder();
            for (String message : messageList) {
                sb.append(message + ", ");
            }
            int messageLength = sb.length();
            if (messageLength >= 2) {
                sb.delete(messageLength - 2, messageLength - 1);
            }
            return sb.toString();
        } else {
            return "Couldn't fetch message from responseObject";
        }
    }

    public void setMessage(Object messageObject) {
        this.message = messageObject;
    }
}
