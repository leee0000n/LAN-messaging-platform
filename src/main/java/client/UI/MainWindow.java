package client.UI;

import client.Client;
import client.MessageHistory;

import message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class MainWindow {

    private static DefaultListModel<String> model;

    public MainWindow() {
            JFrame frame = createFrame();
            addDisconectClientOnClose(frame);

            // Panels
            JPanel roomSelectorPanel = new JPanel();
            JPanel messagingPanel = new JPanel();
            JPanel messageAreaPanel = new JPanel();

            roomSelectorPanel.setBackground(Color.PINK);
            roomSelectorPanel.setPreferredSize(new Dimension(200, 600));
            roomSelectorPanel.setMaximumSize(new Dimension(300, 600));

            messagingPanel.setLayout(new BorderLayout(5, 5));
            messagingPanel.setBackground(Color.orange);

            // Add panels to ui
            frame.add(roomSelectorPanel, BorderLayout.WEST);
            frame.add(messagingPanel, BorderLayout.CENTER);
            messagingPanel.add(messageAreaPanel, BorderLayout.SOUTH);

            // message.Message box to send messages
            JTextArea textArea = createTextArea();
            JScrollPane scrollPane = createScrollPane(textArea);
            // message.Message history
            model = new DefaultListModel<>();
            JScrollPane messageHistoryScrollPane = createMessageHistoryScrollPane();
            JButton sendMessageButton = sendMessageButton(textArea);

            messagingPanel.add(messageHistoryScrollPane, BorderLayout.CENTER);

            // Add message box and button
            messageAreaPanel.add(scrollPane, BorderLayout.SOUTH);
            messageAreaPanel.add(sendMessageButton, BorderLayout.EAST);


    }

    private JFrame createFrame() {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setTitle("Messaging APP");
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout(5,5));

        return frame;
    }

    private void addDisconectClientOnClose(JFrame frame) {
        // Add a window listener
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Code to run when window is closing
                System.out.println("Window is closing!");
                Client.disconnectClient();

                // Close the window after running function
                frame.dispose();
            }
        });
    }

    private JScrollPane createMessageHistoryScrollPane() {
        // The JList that displays the strings
        JList<String> jList = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private JTextArea createTextArea() {
        JTextArea messagingTextArea = new JTextArea(5, 40);
        messagingTextArea.setLineWrap(true);
        messagingTextArea.setWrapStyleWord(true);
        messagingTextArea.setEditable(true);
        messagingTextArea.setBackground(Color.white);

        return messagingTextArea;
    }

    private JScrollPane createScrollPane(JTextArea messagingTextArea) {
        JScrollPane messagingScrollPane = new JScrollPane(messagingTextArea);
        messagingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagingScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        messagingScrollPane.setMaximumSize(new Dimension(350, 500));

        return messagingScrollPane;
    }

    /**
     * Create new send message button
     * @param textArea associated text area
     * @return JButton
     */
    private JButton sendMessageButton(JTextArea textArea) {
        JButton button = new JButton(new AbstractAction("Send Message") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = textArea.getText();

                if (content.isEmpty()) return;

                Client.sendMessage(content);
                MessageHistory.addClientMessage(Client.getName(), content);
                updateMessageDisplay();
                textArea.setText("");
            }
        });

        button.setVisible(true);

        return button;
    }

    public void updateMessageDisplay() {
        ArrayList<Message> messageHistory = MessageHistory.getMessages();
        model.clear();

        for (int i = messageHistory.size() - 1; i >= 0; i--) {
            model.addElement(messageHistory.get(i).getMessage());
        }
    }
}
