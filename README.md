

# Java Chat Application

This is a simple Java-based chat application consisting of a server (`ChatServer`) and multiple clients (`ChatClient`). The server facilitates communication between clients, allowing them to send messages to each other either publicly or privately.


### Running the `ChatServer`

1. Compile the Server: Navigate to the `ChatServer` directory and compile the Java file:
   ```bash
   javac ChatServer.java
   ```

2. Run the Server: Start the `ChatServer` application:
   ```bash
   java ChatServer
   ```
   This will launch the server GUI.

3. Server Interface:
   - Enter a username and click `Connect`.
   - Manage connected clients, view messages, and handle commands.

### Running the `ChatClient`

1. Compile the Client: Navigate to the `ChatClient` directory and compile the Java file:
   ```bash
   javac ChatClient.java
   ```

2. Run the Client: Start the `ChatClient` application:
   ```bash
   java ChatClient
   ```
   This will prompt you to enter the server address.

3. Client Interface:
   - Enter the server address (e.g., `127.0.0.1` for local testing).
   - Enter a username and start chatting.
   - Send messages, use emojis, and execute commands as needed.

## Commands

- **Public Message**: Type a message and press `Send` to broadcast to all clients.
- **Private Message**: Use `@username message` format to send a private message to a specific client.
- **Group Message**: Use `/group create|join|leave|msg groupname message` commands to manage and send messages to groups.
- **List Users**: Use `/list` to see all connected users.
- **Help**: Use `/help` to view available commands.

## Examples

- Sending a public message: `Hello everyone!`
- Sending a private message: `@alice Hi Alice!`
- Creating a group: `/group create developers`
- Joining a group: `/group join developers`
- Sending a group message: `/group msg developers Let's discuss the project.`

