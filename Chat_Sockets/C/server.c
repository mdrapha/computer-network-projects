#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>

#define PORT 12345
#define BUFFER_SIZE 1024

int main()
{
    int serverSocket, clientSocket;
    struct sockaddr_in serverAddr, clientAddr;
    socklen_t addrSize = sizeof(clientAddr);
    char buffer[BUFFER_SIZE];

    // Create socket
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket < 0)
    {
        perror("Error creating socket");
        exit(1);
    }

    // Setup server address
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    // Bind socket
    if (bind(serverSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) < 0)
    {
        perror("Error binding");
        exit(1);
    }

    // Listen
    if (listen(serverSocket, 5) < 0)
    {
        perror("Error listening");
        exit(1);
    }

    printf("Server listening on port %d...\n", PORT);

    // Accept connections
    clientSocket = accept(serverSocket, (struct sockaddr *)&clientAddr, &addrSize);
    if (clientSocket < 0)
    {
        perror("Error accepting connection");
        exit(1);
    }

    printf("Connected to client\n");

    // Chat loop
    while (1)
    {
        memset(buffer, 0, BUFFER_SIZE);
        recv(clientSocket, buffer, BUFFER_SIZE, 0);
        printf("Client: %s", buffer);

        printf("Server: ");
        fgets(buffer, BUFFER_SIZE, stdin);
        send(clientSocket, buffer, strlen(buffer), 0);
    }

    close(serverSocket);
    return 0;
}
