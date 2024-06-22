import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class ChatClient {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private String username;

    public ChatClient(String serverAddress) {
        initializeUI();
        connectToServer(serverAddress);
    }

    private void connectToServer(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 7777);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String username = JOptionPane.showInputDialog(frame, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
            if (username != null && !username.isEmpty()) {
                this.username = username;
                out.println(username);
                new Thread(new IncomingReader()).start();
            } else {
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setTitle("Client");
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
        inputPanel.add(messageField);

        sendButton = new JButton("Send");
        sendButton.addActionListener((ActionEvent e) -> {
            sendMessage();
        });
        inputPanel.add(sendButton);

        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    private void appendToChatArea(String message) {
        chatArea.append(message + "\n");
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = br.readLine()) != null) {
                    appendToChatArea(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = JOptionPane.showInputDialog(null, "Enter server address:", "Server Address", JOptionPane.PLAIN_MESSAGE);
        if (serverAddress != null && !serverAddress.isEmpty()) {
            new ChatClient(serverAddress);
        } else {
            System.exit(0);
        }
    }
}
