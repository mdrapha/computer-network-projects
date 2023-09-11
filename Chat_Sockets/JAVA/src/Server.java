/*  
 *  Server.java
 *  This is the server side of the chat application.
 *  It accepts new connections and creates a new thread for each one.
 *  More info was added to the Readme.md file.
 * 
 *  Authors:
 *  Name: Eduardo Verissimo Faccio            RA: 148.859
 *  Name: Marco Antonio Coral                 RA: 158.467
 *  Name: Raphael Damasceno Rocha de Moraes   RA: 156.380
 */



import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Map<String, PrintWriter> clientMap = new HashMap<>();
    private static Map<String, Boolean> mutedUsers = new HashMap<>();
    private static Set<String> bannedUsers = new HashSet<>();
    private static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) {

        int port = 45678; // default port

        // Check if the user passed a port as an argument 
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]); // if yes, use it
            } catch (NumberFormatException e) {
                System.out.println("> SYSTEM: Porta inválida. Usando porta padrão: 45678");
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Print the server port and IP address
            System.out.println("> SYSTEM: Servidor de chat iniciado na porta " + port);
            System.out.println("> SYSTEM: IP: " + InetAddress.getLocalHost().getHostAddress());

            // Accept new connections and start a new thread for each one
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // add admin user and password to the users map
                users.put("admin", "admin");                

                // Autenticação do usuário
                out.printf("> SERVER: Bem-vindo ao chat. Usuários online: %d\n", clientMap.size());
                // lista de usuarios
                synchronized (clientMap) {
                    for (String user : clientMap.keySet()) {
                        out.println(user);
                    }
                }
                out.println("\n> SERVER: Digite seu nome de usuário:");

                username = in.readLine();
                while (username == null || username.isEmpty()) {
                    out.println("> SERVER: Nome de usuário inválido. Digite outro nome de usuário:");
                    username = in.readLine();
                }

                if (bannedUsers.contains(username)) {
                    out.println("> SERVER: Você foi banido do servidor por um administrador.");
                    return;
                }

                if (users.containsKey(username)) {
                    out.println("> SERVER: Digite sua senha (3 tentativas):");
                    String enteredPassword = in.readLine();
                    if (!users.get(username).equals(enteredPassword)) {
                        out.println("> SERVER: Senha incorreta. (2 tentativas)");
                        enteredPassword = in.readLine();
                        if (!users.get(username).equals(enteredPassword)) {
                            out.println("> SERVER: Senha incorreta. (1 tentativa)");
                            enteredPassword = in.readLine();
                            if (!users.get(username).equals(enteredPassword)) {
                                out.println("> SERVER: Senha incorreta. Você foi desconectado.");
                                return;
                            }
                        }
                    }
                } else {
                    out.println("> SERVER: Você é um novo usuário. Defina sua senha:");
                    String newPassword = in.readLine();
                    users.put(username, newPassword);
                }

                synchronized (clientMap) {
                    clientMap.put(username, out);
                }

                out.println("> SERVER: Bem-vindo, " + username + "!");
                broadcast("> SERVER: " + username + " entrou no chat.");

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/mute")) {
                        if (username.equals("admin")) {
                            String[] tokens = input.split(" ");
                            if (tokens.length == 2) {
                                String userToMute = tokens[1];
                                mutedUsers.put(userToMute, true);
                                out.println("> SERVER: usuario " + userToMute + " mutado.");

                                synchronized (clientMap) {
                                    PrintWriter mutedClient = clientMap.get(userToMute);
                                    if (mutedClient != null) {
                                        mutedClient.println("> SERVER: Você foi mutado pelo administrador.");
                                    }
                                }
                            }
                        } else {
                            out.println("> SERVER: Você não tem permissão para mutar usuários.");
                        }
                    } else if (input.startsWith("/unmute")) {
                        if (username.equals("admin")) {
                            String[] tokens = input.split(" ");
                            if (tokens.length == 2) {
                                String userToUnmute = tokens[1];
                                mutedUsers.remove(userToUnmute);
                                out.println("> SERVER: usuario " + userToUnmute + " desmutado.");

                                synchronized (clientMap) {
                                    PrintWriter mutedClient = clientMap.get(userToUnmute);
                                    if (mutedClient != null) {
                                        mutedClient.println("> SERVER: Você foi desmutado pelo administrador.");
                                    }
                                }
                            }
                        } else {
                            out.println("> SERVER: Você não tem permissão para desmutar usuários.");
                        } 
                    } else if (input.startsWith("/kick")) {
                        if (username.equals("admin")) {

                            String[] tokens = input.split(" ");
                            if (tokens.length == 2) {
                                String userToKick = tokens[1];
                                
                                synchronized (clientMap) {
                                    if(!clientMap.containsKey(userToKick)){ // Verify if the user is on the chat
                                        out.println("> SERVER: O usuário " + userToKick + " não está no chat.");
                                        continue;
                                    }
                                    else if(userToKick.equals("admin")){ // Verify if the user is the admin
                                        out.println("> SERVER: Você não pode kickar o administrador.");
                                        continue;
                                    }
                                
                                    out.println("> SERVER: Você kickou " + userToKick + ".");
                                    broadcast("> SERVER: " + userToKick + " foi kickado pelo administrador.");
                                    PrintWriter kickedClient = clientMap.get(userToKick);
                                    if (kickedClient != null) {
                                        kickedClient.println("> SERVER: Você foi kickado do chat pelo administrador.");
                          
		                        // Close the socket of the kicked user
		                        kickedClient.close();
                                    }
                                    
                                }
                            }
                        } else {
                            out.println("> SERVER: Você não tem permissão para kickar usuários.");
                        }
                    } else if (input.startsWith("/ban")) {
                        if (username.equals("admin")) {
                            String[] tokens = input.split(" ");
                            if (tokens.length == 2) {
                                String userToBan = tokens[1];
                                bannedUsers.add(userToBan);
                                out.println("> SERVER: Você baniu " + userToBan + ".");
                                broadcast("> SERVER: " + userToBan + " foi banido pelo administrador.");
                                synchronized (clientMap) {
                                    PrintWriter bannedClient = clientMap.get(userToBan);
                                    
                                    if (clientMap.containsKey(userToBan)) {
                                        bannedClient.println("> SERVER: Você foi banido do chat pelo administrador.");
                                        bannedClient.close(); // Close input and output streams of the banned user
                                    }
                                }
                            }
                            else{
                                out.println("> SERVER: Comando inválido.");
                            }
                        } else {
                            out.println("> SERVER: Você não tem permissão para banir usuários.");
                        }
                    } else if (input.startsWith("/pm")) {
                        String[] tokens = input.split(" ", 3);
                        if (tokens.length == 3) {
                            String recipient = tokens[1];
                            String message = tokens[2];
                            sendPrivateMessage(username, recipient, message);
                        }
                    } else if (input.startsWith("/exit")) {
                        break; /*
                                * This break is important because you go directly to the finally
                                * block and close the user socket.
                                */
                    } else if (input.startsWith("/help")) {
                        if (username.equals("admin")) {
                            out.println("\n> SERVER: Comandos disponíveis");
                            out.println("/pm <usuário> <mensagem>");
                            out.println("/mute <usuário>");
                            out.println("/unmute <usuário>");
                            out.println("/kick <usuário>");
                            out.println("/ban <usuário>");
                            out.println("/exit\n");
                        } else {
                            out.println("\nComandos disponíveis:");
                            out.println("/pm <usuário> <mensagem>");
                            out.println("/exit - sai do chat\n");
                        }
                    } else {
                        // if message is empty dont broadcast
                        if (input.isEmpty()) {
                            continue;
                        } else if (!mutedUsers.containsKey(username)) { // Verify if the user is muted before
                                                                        // broadcasting his message

                            if (username.equals("admin")) {
                                broadcast("> ADMIN: " + input);
                            } else {
                                broadcast(username + ": " + input);
                            }
                        } else { // dont broadcast
                            continue;
                        }
                    }
                }
            } catch (SocketException e) {
                System.out.println("> SERVER: " + username + " foi desconectado pelo administrador");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientMap) {
                    clientMap.remove(username);
                }
                broadcast("> SERVER: " + username + " saiu do chat.");
            }
        }
    }

    private static void broadcast(String message) {
        synchronized (clientMap) {
            for (PrintWriter client : clientMap.values()) {
                client.println(message);
            }
        }
    }

    private static void sendPrivateMessage(String sender, String recipient, String message) {
        synchronized (clientMap) {
            PrintWriter recipientWriter = clientMap.get(recipient);
            PrintWriter senderWriter = clientMap.get(sender);
            if (recipientWriter != null && senderWriter != null) {
                recipientWriter.println("> PM: (" + sender + "): " + message);
                senderWriter.println("> PM: (" + recipient + "): " + message);
            } else {
                senderWriter.println("> SERVER: Usuário não encontrado ou offline.");
            }
        }
    }
}
