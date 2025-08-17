package server;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import message.Message;

public class Server {

    /**
     * Server socket
     */
    private static ServerSocket serverSocket;

    /**
     * List of all clients, their data and threads
     */
    private final static CopyOnWriteArrayList<ClientThread> clientThreads = new CopyOnWriteArrayList<>();

    /**
     * Buffer of all messages to broadcast to others
     */
    private static volatile ConcurrentLinkedDeque<Message> messagesToBroadcast = new ConcurrentLinkedDeque<>();

    private final static AtomicBoolean serverRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        System.out.println("Starting Server");

        // Get port from arguments, or use default 8888 port
        int port = argsToPortNum(args);

        // Bind application to port specified
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e) {
            System.out.println("Server failed to start. Port " + port + " currently unavailable. Try again.");
            return;
        }
        System.out.println("Server Started\n");
        serverRunning.set(true);


        // Thread for accepting connections
        Thread connectionThread = new Thread(() -> {
            while (serverRunning.get()) {
                try {
                    Socket newSocket = serverSocket.accept(); // Wait for new connection

                    // Create data input and output streams from socket
                    DataInputStream dis = new DataInputStream(newSocket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(newSocket.getOutputStream());

                    // Read in name
                    String name = dis.readUTF();

                    // Create new clientThread object
                    ClientThread clientThread = new ClientThread(name, newSocket, dis, dos);

                    clientThread.start(); // Start the thread
                    clientThreads.add(clientThread);
                    System.out.println("<System Info>: New Client, " + name + ", Connected");
                }
                catch (IOException e) {
                    System.err.println("I/O Exception: " + e.getMessage());
                }
            }
        });

        // Thread for sending input from 1 client to all other clients
        Thread IOThread = new Thread(() -> {
            while (serverRunning.get()) {

                // Do nothing if no messages to broadcast
                if (messagesToBroadcast.isEmpty()) continue;

                for (Message message : messagesToBroadcast) {
                    String clientName = message.getSenderName();
                    System.out.println(message.getMessage());

                    for (ClientThread client : clientThreads) {

                        // If client name matches current client, skip
                        if (client.compareNames(clientName)) continue;

                        client.sendMessage(message);
                    }
                }

                messagesToBroadcast.clear();
            }
        });

        connectionThread.start();
        IOThread.start();

        Scanner input = new Scanner(System.in);
        while (serverRunning.get()) {
            String message = input.nextLine();

            if (message != null) {
                if (message.equalsIgnoreCase("stop")) {
                    serverRunning.set(false);
                }
            }
        }

        closeServer();
    }

    /**
     * Take args passed in and extract port number from them, or default to
     * port 8888 if no port number passed in or port number is invalid
     * @param args args passed to main
     * @return port number to open server socket on
     */
    private static int argsToPortNum(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[0]);

            // Throw exception so it is caught and error message printed
            if (port < 0 || port > 65535) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e) {
            System.err.println("Invalid port number (" + args[0] +"). Defaulting to 8888");
            port = 8888;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("No port number specified. Defaulting to 8888");
            port = 8888;
        }

        return port;
    }

    public static synchronized void addMessageToBroadcast(Message message) {
        messagesToBroadcast.add(message);
    }

    /**
     * Close the server by
     *  - closing all open sockets
     *  - freeing the port used
     */
    private static void closeServer() {
        for (ClientThread client : clientThreads) {
            client.close();
        }

        // Close server socket
        try {
            serverSocket.close();
            serverSocket = null;
        }
        catch (IOException e) {
            System.err.println("I/O Exception: " + e.getMessage());
        }
    }
}

class ClientThread extends Thread {
    private final String clientName;

    private final AtomicBoolean running;
    private final Socket clientSocket;

    private final DataInputStream dis;
    private final DataOutputStream dos;

    /**
     * Initialise client thread with all relevant data. Creates a
     * DataInputStream object
     * @param clientSocket socket for the client
     * @throws IOException if an I/O error occurs when creating the input stream,
     *                     the socket is closed, the socket is not connected,
     *                     or the socket input has been shutdown using shutdownInput()
     */
    public ClientThread(String name, Socket clientSocket, DataInputStream dis, DataOutputStream dos) throws IOException {
        this.running = new AtomicBoolean(true);
        this.running.set(true);

        this.clientName = name;
        this.clientSocket = clientSocket;

        this.dis = dis;
        this.dos = dos;
    }

    /**
     * Separate thread for client socket. Read data from input stream
     * and send any data back to client
     */
    public void run() {
        try {
            while (running.get()) {
                String clientMessage = dis.readUTF();
                Server.addMessageToBroadcast(new Message(clientName, clientMessage));
            }
        }
        catch (EOFException e) {
            System.err.println("Input stream reached end of stream before reading all bytes.");
        }
        catch (UTFDataFormatException e) {
            System.err.println("Bytes do represent UTF-8.");
        }
        catch (IOException e) {
            System.err.println("Client Disconnected.");
        }
    }

    /**
     * Send message to client
     * @param message message to send
     */
    public void sendMessage(Message message) {
        try {
            dos.writeUTF(message.getMessage());
            dos.flush();
        }
        catch (IOException e) {
            System.err.println("I/O Exception in ClientThread: " + e.getMessage());
        }
    }

    public boolean compareNames(String name) {
        return name.equals(this.clientName);
    }

    /**
     * Close client socket
     */
    public void close() {
        try {
            clientSocket.close();
        }
        catch (IOException e) {
            System.err.println("Could not close socket. Closing Client");
        }
    }
}


