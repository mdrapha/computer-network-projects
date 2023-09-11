# Chat Server

This is a simple chat server implemented in Java. It allows users to connect, authenticate, and exchange messages in a chat room. The server supports various commands for muting, kicking, banning, and sending private messages to users.

## Features

- User authentication: Users can create accounts with usernames and passwords or log in if they already have an account.
- Communication: Users can send messages to the chat and see messages from other users.
- Admin privileges: The admin user can mute, unmute, kick, and ban users from the chat.
- Private messaging: Users can send private messages to each other with the command /pm.
- Help command: Users can use the "/help" command to see available commands.

## How to Use

1. Compile the `Server.java` file using a Java compiler.
   ```bash
   javac Server.java
   ```

2. Run the server on the default port (45678):

    ```bash
    java Server
    ```
    
    Or run the server on a custom port:
    
    ```bash
    java Server <port>
    ```

3. Compile the `Client.java` file using a Java compiler.
   ```bash
   javac Client.java
   ```

4. Connect to the server using the client. The client can be run on the same machine as the server or on a different machine. The client can be run using the following commands:

    ```bash
    java Client # If the server is running on the same machine as the client and on the default port (45678)
    ```
    Or the client can be run on a different IP address and/or port:

    ```bash
    java Client <server_ip> <server_port>
    ```

    For example, if the server is running on the same machine as the client, the command would be:

    ```bash
    java Client localhost 45678
    ```

    If the server is running on a different machine, the command would be:

    ```bash
    java Client <server_ip> 45678
    ```

    If the server is running on a different port, the command would be:

    ```bash
    java Client <server_ip> <server_port>
    ```

    For example, if the server is running on a different machine and port 12345, the command would be:

    ```bash
    java Client <server_ip> 12345
    ```


5. Follow the prompts to create an account or log in with an existing account. The admin account is created by default with the username "admin" and password "admin".

6. Once logged in, the user will be connected to the chat room. The user will see messages from other users and can send messages to the chat.

7. Use the available commands to interact with the chat server.

### Commands
- /pm <username> <message>: Send a private message to a specific user.
- /mute <username>: Mute a user (admin-only).
- /unmute <username>: Unmute a user (admin-only).
- /kick <username>: Kick a user from the chat (admin-only).
- /ban <username>: Ban a user from the chat (admin-only).
- /exit: Exit the chat.

## Note
- The server supports only a single chat room.
- Admin privileges are given to the "admin" user by default. You can customize this in the code.
- This is a basic implementation and may require further enhancements and error handling for production use.
