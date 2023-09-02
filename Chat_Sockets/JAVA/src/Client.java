import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 49165); // Connect to server on localhost
            
            // Input and Output streams for communication
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
            
            System.out.print("System: Connected to server!\nSystem: What is your name? ");
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String name = stdIn.readLine(); // Read the user's name from the console
            
            // Make sure the user enters a valid name
            while(name == null || name.isEmpty()) {
                System.out.println("System: Invalid name!");
                System.out.print("System: What is your name? ");

                name = stdIn.readLine();
            }
            
            System.out.println("System: Welcome, " + name + "!");
            
            out.println(name); // Send the user's name to the server

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

                    if(!socket.isClosed()){
                        socket.close();
                    }
                } 
                catch (SocketException e) {
                    /* Catch SocketException when the server closes the connection
                       This is not an error, so we don't need to print the stack trace */
                    
                }
                catch (IOException e) {
                    System.out.println("System: An error happened while reading from the server!");
                }
                finally{
                    System.out.println("System: Chat closed!");
                    System.exit(0);
                }
            });
            serverThread.start();

            // Read and send user messages to the server
            while (true) {
                // See if the server is still connected
                if (socket.isClosed()) {
                    System.out.println("System: Chat closed!");
                    break;
                }

                String userMessage = stdIn.readLine();
                if (userMessage == null || userMessage.equalsIgnoreCase("/exit")) {
                    // User wants to exit
                    System.out.println("System: Closing connection...");
                    break;
                }

                out.println(userMessage);
            }

            // Close the socket
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("System: An error happened while connecting to the server!");
            System.out.println("System: Make sure the server is running and try again.");
        }
        finally{
            System.exit(0);
        }
    }
}
