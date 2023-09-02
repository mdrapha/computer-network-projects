import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 49165); // Connect to server on localhost
            
            // Input and Output streams for communication
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Connected to server!\nWhat is your name? ");
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String name = stdIn.readLine();

            // Send the user's name to the server
            out.println(name);

            // Create a thread to read messages from the server
            Thread serverThread = new Thread(() -> {
                try {
                    while (true) {
                        String serverResponse = serverIn.readLine();
                        if (serverResponse == null) {
                            // Server has closed the connection
                            break;
                        }
                        System.out.println(serverResponse);
                    }
                // Se a excessão for de socket close, apenas avisar que o chat foi encerrado, outras excessões devem ser mostradas o stack trace    
                } 
                catch (SocketException e) {
                    System.out.println("Chat closed!");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();

            // Read and send user messages to the server
            while (true) {
                String userMessage = stdIn.readLine();
                if (userMessage == null || userMessage.equalsIgnoreCase("/exit")) {
                    // User wants to exit
                    System.out.println("Closing connection...");
                    break;
                }
                out.println(userMessage);
            }

            // Close the socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
