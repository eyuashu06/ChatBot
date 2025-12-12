
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatClientGUI {

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public ChatClientGUI() {
        createAndShowGUI();
        connectToServer();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Client");
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
        messageField.setToolTipText("Type your message here...");
        messageField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(90, 40));
        sendButton.setBackground(new Color(0x4CAF50));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        bottomBar.add(messageField, BorderLayout.CENTER);
        bottomBar.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomBar, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeConnection();
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String msg;
                while ((msg = in.readLine()) != null) {
                    final String messageFromServer = msg;
                    SwingUtilities.invokeLater(() ->
                            chatArea.append("Server: " + messageFromServer + "\n")
                    );
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> chatArea.append("Connection failed.\n"));
            }
        }).start();
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        chatArea.append("You: " + msg + "\n");

        if (out != null) out.println(msg);

        messageField.setText("");
    }

    private void closeConnection() {
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
