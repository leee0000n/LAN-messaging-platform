package message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private final String message;
    private final String senderName;
    private Date date;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy | HH:mm");

    public Message(String senderName, String message) {
        this.message = message;
        this.senderName = senderName;
        this.date = new Date();
    }

    /**
     * Convert message string into a message object
     * @param messageString message string from server
     */
    public Message(String messageString) {
        // Example input: "Alice [2025-08-16 14:30:00] : Hello world"
        int nameEnd = messageString.indexOf(" [");
        int dateStart = nameEnd + 2;
        int dateEnd = messageString.indexOf("] :", dateStart);
        int msgStart = dateEnd + 3;

        senderName = messageString.substring(0, nameEnd);
        String dateStr = messageString.substring(dateStart, dateEnd);
        date = new Date();
        message = messageString.substring(msgStart);
        try {
            date = sdf.parse(dateStr);
        }
        catch (ParseException e) {
            System.err.println("Error parsing date: " + dateStr);
        }

    }

    public String getMessage() {
        return senderName + " [" + sdf.format(date) + "] : " + message;
    }

    public String getSenderName() {
        return senderName;
    }
}
