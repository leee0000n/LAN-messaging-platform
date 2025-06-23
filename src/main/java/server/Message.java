package server;

import java.util.Date;

public class Message {
    private String message;
    private String senderName;
    private Date date;

    public Message(String senderName, String message) {
        this.message = message;
        this.senderName = senderName;
        this.date = new Date();
    }

    public String getMessage() {
        return senderName + " [" + date.toString() + "] : " + message;
    }

    public String getSenderName() {
        return senderName;
    }
}
