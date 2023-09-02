import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 49165;
    // Map to store client usernames and their corresponding output streams
    private static Map<String, PrintWriter> clientStreams = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());

                // Input and Output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Read the client's username
                String username = in.readLine();
                if (username != null) {
                    synchronized (clientStreams) {
                        clientStreams.put(username, out);
                        for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                            if (!entry.getKey().equals(username)) {
                                PrintWriter writer = entry.getValue();
                                writer.println(username + " connected!");
                            }
                        }
                    }
                    
                    // Create a new thread to handle client communication
                    ClientHandler clientHandler = new ClientHandler(socket, in, username);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server closed!");
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private String username;

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
                        for (Map.Entry<String, PrintWriter> entry : clientStreams.entrySet()) {
                            if (!entry.getKey().equals(username)) {
                                PrintWriter writer = entry.getValue();
                                writer.println(username + " said: " + clientMessage);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
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
                        writer.println(username + " disconnected!");
                    }
                }

                System.out.println("Client disconnected: " + username);
            }
        }
    }
}
