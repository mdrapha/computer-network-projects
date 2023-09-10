/*  Client.java
 *  This is the client side of the chat application.
 *  It connects to the server and sends and receives messages.
 * 
 *  Authors:
 *  Name: Eduardo Verissimo Faccio            RA: 148.859
 *  Name: Marco Antonio Coral                 RA: 158.467
 *  Name: Raphael Damasceno Rocha de Moraes   RA: 156.380
 */

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 45678;

        // If the user passed the server address and port as arguments, use them

        try{
            if (args.length == 2) {
                serverAddress = args[0];
                serverPort = Integer.parseInt(args[1]);
            } else if ( args.length == 0) {
                System.out.println("> CLIENT: Argumentos não passados. Usando valores padrões.");
            } else {
                System.out.println("> CLIENT: Argumentos inválidos. Usando valores padrões.");
                System.out.println("> CLIENT: java Client <server_address> <server_port>");
            }
        } catch (NumberFormatException e) {
            System.out.println("> CLIENT: Número de porta inválido. Usando valor padrão.");
        }
        
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
