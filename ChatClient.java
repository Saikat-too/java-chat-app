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
    private String username;

    public ChatClient() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame();
        frame.setTitle("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
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

        JButton sendButton = new JButton("Send");
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

                // Establish connection with the server
                try {
                    System.out.println("Sending request to the server");
                    socket = new Socket("127.0.0.1", 7777);
                    appendToChatArea("[Client]: Connected to server");

                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());
                    out.println(username); // Send username to server
                    out.flush();
                    startReading();
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageField.requestFocus();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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

    private void appendToChatArea(String message) {
        chatArea.append(message + "\n");
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            out.flush();
            messageField.setText("");
        }
    }

    private void shutdown() {
        try {
            socket.close();
            br.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void startReading() {
        Runnable reader = () -> {
            try {
                while (true) {
                    String message = br.readLine();
                    appendToChatArea(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(reader).start();
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}
