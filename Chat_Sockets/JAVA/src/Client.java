import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Boolean running = true;

            Socket socket = new Socket("localhost", 49165); // Connect to server on localhost
            
            // Input and Output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Connected to server!\nWhat is your name? ");
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String name = stdIn.readLine();

            // Send a message to the server
            out.println(name);
            

            while(running){

                if(in.ready()){
                    String serverResponse = in.readLine();
                    System.out.println("Server response: " + serverResponse);
                
                }
            }
            // Close the streams and socket
            in.close();
            out.close();
            socket.close();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
