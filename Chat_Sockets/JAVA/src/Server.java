import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
     
        try {
            ServerSocket serverSocket = new ServerSocket(49165); // Create a server socket on port 49165
            System.out.println("Server started on port 49165");
            Boolean running = true;
            while (running) {
                Socket socket = serverSocket.accept(); // Accept a connection from a client
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
                
                // Input and Output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                // Receive and print client's message
                String clientMessage = in.readLine();
                System.out.println("Client message: " + clientMessage);
                
                // Send a response message to the client
                out.println("Hello " + clientMessage + "!");
                
                // Close the streams and socket
                in.close();
                out.close();
                socket.close();
            } 
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
           
        }
        finally {
            System.out.println("Server closed!");
        }
    }
}
