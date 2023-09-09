import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static Map<String, PrintWriter> clientMap = new HashMap<>();
    private static Map<String, Boolean> mutedUsers = new HashMap<>();
    private static Set<String> bannedUsers = new HashSet<>();
    
    public static void main(String[] args) {
        int port = 45678; // default port for the chat server
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("> SYSTEM: Servidor de chat iniciado na porta " + port);

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
                while (username == null || username.isEmpty() || clientMap.containsKey(username)) {
                    out.println("> SERVER: Nome de usuário inválido. Digite outro nome de usuário:");
                    username = in.readLine();
                }
                if (bannedUsers.contains(username))
                {
                    out.println("> SERVER: Você foi banido do servidor por um administrador.");
                    return;
                }
                
                if (username.equals("admin")) {
                    out.println("> SERVER: Digite a senha (3 tentativas):");
                    String adminPassword = in.readLine();
                    if (!adminPassword.equals("admin")) {
                        out.println("> SERVER: Senha incorreta (2 tentativas):");
                        adminPassword = in.readLine();
                        if (!adminPassword.equals("admin")) {
                            out.println("> SERVER: Senha incorreta (1 tentativa):");
                            adminPassword = in.readLine();
                            if (!adminPassword.equals("admin")) {
                                out.println("> SERVER: Senha incorreta. Você foi desconectado.");
                                return;
                            }
                        }
                    }
                }
                
                synchronized (clientMap) {
                    clientMap.put(username, out);
                }
                
                out.println("> SERVER: Bem-vindo, " + username + "!");
                broadcast("> SERVER: " + username + " entrou no chat.");
                
                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/mute")) {
                        if(username.equals("admin")) {
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
                        if(username.equals("admin")) {
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
                        if(username.equals("admin")) {
                            //TODO implementar kick
                        } else {
                            out.println("> SERVER: Você não tem permissão para kickar usuários.");
                        }
                    } 
                    else if (input.startsWith("/ban")) {
                        if(username.equals("admin")){
                            String[] tokens = input.split(" ");
                            if (tokens.length == 2) {
                                String userToBan = tokens[1];
                                bannedUsers.add(userToBan);
                                out.println("> SERVER: Você baniu " + userToBan + ".");
                                broadcast("> SERVER: " + userToBan + " foi banido pelo administrador.");
                                synchronized (clientMap) {
                                    PrintWriter bannedClient = clientMap.get(userToBan);
                                    if (bannedClient != null) {
                                        bannedClient.println("> SERVER: Você foi banido do chat pelo administrador.");
                                    }
                                }
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
                    } else if (input.startsWith("/exit")){
                        break; /* This break is important because you go directly to the finally 
                                block and close the user socket. */
                    } else if (input.startsWith("/help")) {
                        if(username.equals("admin")) {
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
                        } else if (!mutedUsers.containsKey(username)) { // Verify if the user is muted before broadcasting his message

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