package client;

import message.Message;

import java.util.ArrayList;

public class MessageHistory {
    private static final ArrayList<Message> messages = new ArrayList<>();
    private static int messageHistorySize = 1000;
    private static int earliestMessage = 0;

    /**
     * @param messageHistorySize new message history size
     */
    public static void setMessageHistorySize(int messageHistorySize) {
        MessageHistory.messageHistorySize = messageHistorySize;
    }

    /**
     * Add new message to message history. Replaces earliest message
     * in history if messageHistorySize reached.
     * @param message message to add
     */
    public static void addClientMessage(String senderName, String message) {
        Message newMessage = new Message(senderName, message);

        // If message history maxed out, replace the earliest message
        if (messages.size() == messageHistorySize) {
            messages.set(earliestMessage, newMessage);
            earliestMessage = (earliestMessage + 1) % messageHistorySize;
            return;
        }

        messages.add(newMessage);
    }

    public static void addIncomingMessage(String message) {
        Message newMessage = new Message(message);

        // If message history maxed out, replace the earliest message
        if (messages.size() == messageHistorySize) {
            messages.set(earliestMessage, newMessage);
            earliestMessage = (earliestMessage + 1) % messageHistorySize;
            return;
        }

        messages.add(newMessage);
    }

    /**
     * @return message history in order of earliest first, latest last
     */
    public static ArrayList<message.Message> getMessages() {
        ArrayList<Message> rtn = new ArrayList<>();
        int index = earliestMessage;

        for (int i = 0; i < messages.size(); i++) {
            rtn.add(messages.get(index));
            index = (index + 1) % messageHistorySize;
        }

        return rtn;
    }
}
