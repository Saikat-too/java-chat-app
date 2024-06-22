import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

public class ChatServer {

    private ServerSocket server;
    private Set<ClientHandler> clientHandlers;
    private Map<String, ClientHandler> clients;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String username;

    public ChatServer() {
        clientHandlers = new HashSet<>();
        clients = new HashMap<>();
        initializeUI();
        startServer();
    }

    private void startServer() {
        try {
            server = new ServerSocket(7777);
            appendToChatArea("Server is ready to accept connections...");
            appendToChatArea("Waiting...");

            while (true) {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setTitle("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.getContentPane().setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setLayout(new GridLayout(0, 2, 0, 0));

        messageField = new JTextField();
        messageField.setEnabled(false);
        inputPanel.add(messageField);

        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener((ActionEvent e) -> {
            sendMessage();
        });
        inputPanel.add(sendButton);

        JLabel usernameLabel = new JLabel("Username:");
        inputPanel.add(usernameLabel);

        JTextField usernameField = new JTextField();
        inputPanel.add(usernameField);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener((ActionEvent e) -> {
            String usernameInput = usernameField.getText();
            if (!usernameInput.isEmpty()) {
                username = usernameInput;
                connectButton.setEnabled(false);
                usernameField.setEditable(false);
                messageField.setEnabled(true);
                sendButton.setEnabled(true);
                messageField.requestFocus();
            }
        });
        inputPanel.add(connectButton);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener((ActionEvent e) -> {
            shutdown();
        });
        inputPanel.add(quitButton);

        frame.setVisible(true);
    }

    private void shutdown() {
        try {
            server.close();
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void appendToChatArea(String message) {
        chatArea.append(message + "\n");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            broadcastMessage(username + " [" + timestamp + "]: " + message);
            messageField.setText("");
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    private void updateUserList() {
        StringBuilder userList = new StringBuilder("[UserList]");
        for (String user : clients.keySet()) {
            userList.append(user).append(",");
        }
        broadcastMessage(userList.toString());
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader br;
        private PrintWriter out;
        private String clientUsername;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // First message should be the username
                clientUsername = br.readLine();
                clients.put(clientUsername, this);
                updateUserList();
                appendToChatArea(clientUsername + " has joined the chat.");
                broadcastMessage(clientUsername + " has joined the chat.");

                String message;
                while ((message = br.readLine()) != null) {
                    if (message.startsWith("@")) {
                        privateMessage(message);
                    } else if (message.startsWith("/")) {
                        handleCommand(message);
                    } else {
                        appendToChatArea(message);
                        broadcastMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clients.remove(clientUsername);
                    clientHandlers.remove(this);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void privateMessage(String message) {
            int firstSpaceIndex = message.indexOf(" ");
            if (firstSpaceIndex != -1) {
                String recipient = message.substring(1, firstSpaceIndex);
                String privateMessage = message.substring(firstSpaceIndex + 1);
                ClientHandler recipientHandler = clients.get(recipient);
                if (recipientHandler != null) {
                    recipientHandler.sendMessage("[Private] " + clientUsername + ": " + privateMessage);
                    sendMessage("[Private] to " + recipient + ": " + privateMessage);
                } else {
                    sendMessage("User " + recipient + " not found.");
                }
            } else {
                sendMessage("Invalid private message format.");
            }
        }

        private void handleCommand(String command) {
            if (command.equals("/list")) {
                sendMessage("[UserList] " + String.join(", ", clients.keySet()));
            } else if (command.equals("/help")) {
                sendMessage("[Help] Available commands: /list, /help, @username message");
            } else {
                sendMessage("Unknown command: " + command);
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                br.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
