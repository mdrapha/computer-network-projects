// English only please

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 45678;

        try (
            Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)); 
        ) {
            
            // Initialize the Thread that are going to listen for new messages from the server
            new Thread(() -> {
                String input;
                try {
                    while ((input = in.readLine()) != null){
                        System.out.println(input);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    System.out.println("> SYSTEM: Connection closed.");
                    System.exit(0);
                }

            }).start();

            // Read the user input and send it to the server
            try {
                while(true) {
                    // Read the user input and send it to the server
                    String userInput = stdin.readLine();
                    out.println(userInput);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                System.out.println("> SYSTEM: Connection closed.");
                System.exit(0);
            }
            
        } catch (IOException e) {
            System.out.println("> SERVER: Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
