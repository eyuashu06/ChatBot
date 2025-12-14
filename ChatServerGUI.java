import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServerGUI {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    public ChatServerGUI() {
        createAndShowGUI();
        startServer();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 550);
        frame.setLayout(new BorderLayout(10, 10));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(0xF4F4F4));
        chatArea.setFont(chatArea.getFont().deriveFont(15f));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel bottomBar = new JPanel(new BorderLayout(5, 5));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(0, 40));
        messageField.setToolTipText("Type a message to broadcast...");
        messageField.addActionListener(e -> sendToAll());

        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(90, 40));
        sendButton.setBackground(new Color(0x2196F3));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendToAll());

        bottomBar.add(messageField, BorderLayout.CENTER);
        bottomBar.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomBar, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                updateChat("📡 Server started on port 5000...\n");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    updateChat("🔵 Client connected: " + clientSocket.getInetAddress() + "\n");

                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    handler.start();
                }

            } catch (Exception e) {
                updateChat("❌ Server error: " + e.getMessage() + "\n");
            }
        }).start();
    }

    private void sendToAll() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        updateChat("Server: " + msg + "\n");

        for (ClientHandler client : clients) {
            client.send(msg);
        }

        messageField.setText("");
    }

    private void updateChat(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message));
    }

    // ============ Client Handler ============
    class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                updateChat("⚠ Error setting up client: " + e.getMessage() + "\n");
            }
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    updateChat("Client: " + msg + "\n");
                    broadcast("Client: " + msg);
                }
            } catch (Exception e) {
                updateChat("🔴 Client disconnected.\n");
            } finally {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }

        public void send(String msg) {
            out.println(msg);
        }

        public void broadcast(String msg) {
            for (ClientHandler client : clients) {
                client.send(msg);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatServerGUI::new);
    }
}
