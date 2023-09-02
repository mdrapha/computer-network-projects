import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 49165;
    // Map to store client usernames and their corresponding output streams
    private static Map<String, PrintWriter> clientStreams = new HashMap<>();
    private static Map<String, Socket> clientSockets = new HashMap<>();
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        
        // Create a server socket
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            // Input stream for server commands
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // Create a thread to handle server commands
            Thread serverHandleThread = new Thread(new ServerHandle(stdIn));
            serverHandleThread.start();
        }
        catch (IOException e) {
            System.out.println("System: An error happened while starting the server!");
            //e.printStackTrace();
            return;
        }        
        
        Socket socket = null;
        // Accept client connections
        try {
            while (true) {
                socket = serverSocket.accept();

                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                
                // Input and Output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Read the client's username
                String username = in.readLine();
                if (username != null) {
                    synchronized (clientStreams) {
                        // Check if the username is already taken
                        if (clientStreams.containsKey(username)) {
                            System.out.println("System: Username already taken!");
                            out.println("System: Username already taken!");
                            socket.close();
                            continue;
                        }

                        clientSockets.put(username, socket); // Add the client's socket to the map
                        clientStreams.put(username, out); // Add the client's output stream to the map

                        for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                            if (!entry.getKey().equals(username)) {
                                PrintWriter writer = entry.getValue();
                                writer.println("System: " + username + " connected!");
                            }
                        }
                    }
                    
                    // Create a new thread to handle client communication
                    ClientHandler clientHandler = new ClientHandler(socket, in, username);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            }
        } catch (SocketException e) {
            // Catch SocketException when the server closes the connection
            System.out.println("Application closed!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thread to handle client communication
    private static class ClientHandler implements Runnable {
        private Socket socket; // Client socket
        private BufferedReader in; // Input stream
        private String username; // Client username

        public ClientHandler(Socket socket, BufferedReader in, String username) {
            this.socket = socket;
            this.in = in;
            this.username = username;
        }

        @Override
        public void run() {
            try {
                String clientMessage;

                while ((clientMessage = in.readLine()) != null) {
                    System.out.println(username + " said: " + clientMessage);

                    // Send the received message to all clients except the sender
                    synchronized (clientStreams) {
                        // Iterate over the map and send the message to all clients except the sender
                        for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                            if (!entry.getKey().equals(username)) {
                                PrintWriter writer = entry.getValue();
                                writer.println(username + " said: " + clientMessage);
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                // Catch SocketException when the server closes the connection
                return; // Stop the thread
            } catch (IOException e) {
                e.printStackTrace();
            } 

            // Close the client socket and remove the client's output stream from the map
            try {
                if(!socket.isClosed()){
                    socket.close();
                }   
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Remove the client's output stream from the map
            synchronized (clientStreams) {
                clientStreams.remove(username);
            }
            
            // Notify all clients that the user has disconnected
            synchronized (clientStreams) {
                for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                    PrintWriter writer = entry.getValue();
                    writer.println("System: " + username + " disconnected!");
                }
            }

            System.out.println("Client disconnected: " + username);
            
        }
    }

    private static class ServerHandle implements Runnable {
        private BufferedReader in;

        public ServerHandle(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = this.in.readLine()) != null) {
                    if(serverMessage.equalsIgnoreCase("/exit")) {
                        this.closeSockets(); // Close all client sockets and the server socket
                        break;  
                    }
                    else if(serverMessage.equalsIgnoreCase("/list")) {
                        synchronized (clientStreams) {
                            System.out.println("Server: Connected clients:");
                            for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                                System.out.println(entry.getKey());
                            }
                        }
                    }
                    else if(serverMessage.equalsIgnoreCase("/kick")){
                        System.out.println("Server: Who do you want to kick?");
                        String kick = this.in.readLine();
                        synchronized (clientSockets) {
                            if(clientSockets.containsKey(kick)){
                                Socket socket = clientSockets.get(kick);
                                socket.close();
                                clientSockets.remove(kick);
                                clientStreams.remove(kick);
                                System.out.println("Server: " + kick + " has been kicked!");
                            }
                            else{
                                System.out.println("Server: " + kick + " is not connected!");
                            }
                        }
                    }
                    else if(serverMessage.equalsIgnoreCase("/help")) {
                        System.out.println("Server: Commands:");
                        System.out.println("/list - List all connected clients");
                        System.out.println("/exit - Close the server");
                        System.out.println("/kick - Kick a client");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        private void closeSockets() {
            System.out.println("Server closing connection...");

            // Close all client sockets
            synchronized (clientStreams) {
                // Iterate over the map and send the message to all clients that the server is closing
                for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                    PrintWriter writer = entry.getValue();
                    writer.println("System: Server is making a shutdown!");
                    writer.close();
                }
                // Close all client sockets
                for (Map.Entry<String, Socket> entry : clientSockets.entrySet()) {
                    Socket socket = entry.getValue();
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                System.out.println("All clients disconnected!");

                // Close the server socket
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Server closed successfully!");
        }
    }
}
