package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;

    private final static AtomicBoolean clientConnected = new AtomicBoolean(false);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask for a name
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("\n\n");

        socket = openSocket(args);

        if (socket == null) {
            System.err.println("Could not open socket");
            return;
        }

        // Create data input and data output streams
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
            return;
        }

        // Send name to server
        writeToDataOutput(name);
        clientConnected.set(true);

        Thread inputThread = new Thread(() -> {
            try {
                while (clientConnected.get()) {
                    System.out.println("\r" + dis.readUTF());
                    System.out.print(" > ");
                }
            }
            catch (IOException e) {
                System.err.println("I/O Exception: " + e.getMessage());
            }
        });

        inputThread.start();

        while (clientConnected.get()) {
            String message;

            System.out.print(" > ");

            message = scanner.nextLine();

            if ("quit".equalsIgnoreCase(message)) {
                System.out.println("Bye!");
                break;
            }

            if (!writeToDataOutput(message)) System.err.println("Failed to send message");
        }

        scanner.close();

        closeConnection();
    }

    private static Socket openSocket(String[] args) {
        String ip;
        int port;

        // Validate port and ip address
        try {
            // Validate ip
            ip = args[0];
            if (!validateIp(ip)) {
                System.out.println("Invalid IP address");
                return null;
            }

            // Validate port number
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) {
                System.out.println("Invalid port number");
                return null;
            }
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid port number. Cannot connect to a server");
            return null;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Too few arguments. Expected 2, ip and then port but only got " + args.length);
            return null;
        }

        Socket socket;
        try {
            socket = new Socket(ip, port);
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host: " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
            return null;
        }
        catch (SecurityException e) {
            System.out.println("Security error: " + e.getMessage());
            return null;
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
        if (outputStr == null) return false;

        try {
            dos.writeUTF(outputStr);
            dos.flush();
        }
        catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            clientConnected.set(false);
            return false;
        }

        return true;
    }

    private static void closeConnection() {
        try {
            socket.close();
            dis.close();
        }
        catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
