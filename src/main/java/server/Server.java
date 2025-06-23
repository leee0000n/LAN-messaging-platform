package server;

/*
✅ 2. Server Responsibilities
Create a Server Socket → Binds to a specific port on your machine.

Listen for client connections (infinite or limited loop).

Accept a client → Creates a Socket specific to that connection.

Handle communication → Usually on a new thread per client (so multiple clients can connect).

Send/receive messages → Via Input/Output streams.

Close connections when done.
 */

import client.Client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    /**
     * Server socket
     */
    private static ServerSocket serverSocket;

    /**
     * Max time to block for when connecting to client
     */
    private final static int ACCEPT_MAX_WAIT_TIME = 10000;

    /**
     * List of all clients, their data and threads
     */
    private static CopyOnWriteArrayList<ClientThread> clientThreads = new CopyOnWriteArrayList<>();

    /**
     * Buffer of all messages to broadcast to others
     */
    private static volatile ConcurrentLinkedDeque<Message> messagesToBroadcast = new ConcurrentLinkedDeque<>();

    private static volatile String consoleInput = null;
    private static AtomicBoolean serverRunning = new AtomicBoolean(false);

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
                    e.printStackTrace();
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
                        if (!client.compareNames(clientName)) continue;

                        client.addMessage(message);
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
                if (message.equals("stop")) {
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
        // TODO: Free all open sockets


        // Close server socket
        try {
            serverSocket.close();
            serverSocket = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Clean up other resources
    }
}

class ClientThread extends Thread {
    private String clientName;

    private AtomicBoolean running;
    private Socket clientSocket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private ConcurrentLinkedDeque<Message> messagesToSend;

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

        this.messagesToSend = new ConcurrentLinkedDeque<>();

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

                // Send messages if there are any in arraylist
                if (!messagesToSend.isEmpty()) {
                    for (Message message : messagesToSend) {
                        sendMessage(message);
                    }

                    // Clear messages since they are all sentM
                    messagesToSend.clear();
                }
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
    private void sendMessage(Message message) {
        try {
            dos.writeUTF(message.getMessage());
            dos.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a message to list of messages to send
     * @param message message to send
     */
    public synchronized void addMessage(Message message) {
        if (message == null) System.err.println("Message is null");
        messagesToSend.add(message);
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


