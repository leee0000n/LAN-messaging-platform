package client;

import client.UI.Launcher;
import client.UI.MainWindow;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {

    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;

    private final static String name = getRandomString();
    private static boolean clientConnected = false;

    public static MainWindow mainWindow;

    public static void disconnectClient() {
        closeConnection();
        clientConnected = false;
    }

    public static void sendMessage(String message) {
        if (!writeToDataOutput(message)) System.err.println("Failed to send message");
    }

    private static String getRandomString() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder rndStrBuilder = new StringBuilder();
        Random rnd = new Random();
        while (rndStrBuilder.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * CHARS.length());
            rndStrBuilder.append(CHARS.charAt(index));
        }
        return rndStrBuilder.toString();
    }

    public static void main(String[] args) {
        Launcher.UILauncher();

        socket = openSocket(args);

        if (socket == null) {
            System.err.println("Could not open socket");
            System.exit(1);
        }

        // Create data input and data output streams
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
            System.exit(1);
        }

        clientConnected = true;

        // Send name to server
        writeToDataOutput(name);

        Thread incomingDataThread = new Thread(() -> {
            while (clientConnected) {
                try {
                    String incomingMessage = dis.readUTF();
                    MessageHistory.addIncomingMessage(incomingMessage);
                    SwingUtilities.invokeLater(() -> mainWindow.updateMessageDisplay());
                }
                catch (IOException e) {
                    System.err.println("I/O Exception when reading incoming message");
                }
            }
        });

        incomingDataThread.start();
    }

    private static Socket openSocket(String[] args) {
        String ip = "";
        int port = 0;

        // Validate port and ip address
        try {
            // Validate ip
            ip = args[0];
            if (!validateIp(ip)) {
                System.out.println("Invalid IP address");
                System.exit(1);
            }

            // Validate port number
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) {
                System.out.println("Invalid port number");
                System.exit(1);
            }
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Cannot connect to a server");
            System.exit(1);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Too few arguments. Expected 2, ip and then port but only got " + args.length);
            System.exit(1);
        }

        Socket socket = null;
        try {
            socket = new Socket(ip, port);
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host: " + e.getMessage());
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
        catch (SecurityException e) {
            System.out.println("Security error: " + e.getMessage());
            System.exit(1);
        }

        return socket;
    }

    private static boolean validateIp(String ip) {
        if (ip.equals("localhost")) return true;

        String[] numbers = ip.split("\\.");
        if (numbers.length != 4) return false;

        try {
            for (int i = 0; i < 4; i++) {
                if (Integer.parseInt(numbers[i]) > 255) return false;
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    private static boolean writeToDataOutput(String outputStr) {
        if (outputStr == null) return true;

        try {
            dos.writeUTF(outputStr);
            dos.flush();
        }
        catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            return false;
        }

        return true;
    }

    private static void closeConnection() {
        try {
            socket.close();
            dis.close();
            dos.close();
        }
        catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    public static String getName() {
        return name;
    }
}
