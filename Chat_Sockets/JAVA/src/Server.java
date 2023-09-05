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
            System.out.println("Servidor de chat iniciado na porta " + port);

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
                out.printf("Bem-vindo ao chat. Usuários online: %d\n", clientMap.size());
                out.println("Digite seu nome de usuário:");
                
                username = in.readLine();
                if (username == null || username.isEmpty() || clientMap.containsKey(username) || bannedUsers.contains(username)) {
                    out.println("Nome de usuário inválido. Conexão encerrada.");
                    return;
                }
                
                out.println("Digite sua senha:");
                String password = in.readLine();
                // TODO: check if the password is correct
                
                synchronized (clientMap) {
                    clientMap.put(username, out);
                }
                
                out.println("Bem-vindo, " + username + "!");
                broadcast(username + " entrou no chat.");
                
                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/mute")) {
                        String[] tokens = input.split(" ");
                        if (tokens.length == 2) {
                            String userToMute = tokens[1];
                            mutedUsers.put(userToMute, true);
                            out.println("Você mutou " + userToMute + ".");
                        }
                    } else if (input.startsWith("/ban")) {
                        String[] tokens = input.split(" ");
                        if (tokens.length == 2) {
                            String userToBan = tokens[1];
                            bannedUsers.add(userToBan);
                            out.println("Você baniu " + userToBan + ".");
                            broadcast(userToBan + " foi banido pelo administrador.");
                            synchronized (clientMap) {
                                PrintWriter bannedClient = clientMap.get(userToBan);
                                if (bannedClient != null) {
                                    bannedClient.println("Você foi banido do chat pelo administrador.");
                                }
                            }
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
                        out.println("Comandos disponíveis:");
                        out.println("/mute <usuário> - muta um usuário");
                        out.println("/ban <usuário> - bane um usuário");
                        out.println("/pm <usuário> <mensagem> - envia uma mensagem privada para um usuário");
                        out.println("/exit - sai do chat");
                    } else {
                        // Verify if the user is muted before broadcasting his message
                        if (!mutedUsers.containsKey(username)) {
                            broadcast(username + ": " + input);
                        } else {
                            out.println("Você está mutado e não pode enviar mensagens.");
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
                broadcast(username + " saiu do chat.");
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
                recipientWriter.println("Mensagem privada de " + sender + ": " + message);
                senderWriter.println("Mensagem privada para " + recipient + ": " + message);
            } else {
                senderWriter.println("Usuário não encontrado ou offline.");
            }
        }
    }
}
