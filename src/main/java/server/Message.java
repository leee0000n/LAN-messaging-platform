package server;

import java.util.Date;

public class Message {
    private final String message;
    private final String senderName;
    private final Date date;

    public Message(String senderName, String message) {
        this.message = message;
        this.senderName = senderName;
        this.date = new Date();
    }

    public String getMessage() {
        return senderName + " [" + date + "] : " + message;
    }

    public String getSenderName() {
        return senderName;
    }
}
