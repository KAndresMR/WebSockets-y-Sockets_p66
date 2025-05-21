import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class ClienteChat {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JFrame ventana;
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton botonEnviar;

    private String host;
    private int puerto;

    public ClienteChat(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;

        // Crear GUI en hilo de eventos Swing
        SwingUtilities.invokeLater(() -> {
            crearGUI();
            // Abrir conexión y empezar a escuchar mensajes en hilo aparte
            new Thread(() -> conectarYEscuchar()).start();
        });
    }

    private void crearGUI() {
        ventana = new JFrame("Cliente Chat");
        ventana.setSize(400, 400);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaChat);

        campoMensaje = new JTextField();
        botonEnviar = new JButton("Enviar");

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);

        ventana.getContentPane().add(scroll, BorderLayout.CENTER);
        ventana.getContentPane().add(panelInferior, BorderLayout.SOUTH);

        botonEnviar.addActionListener(e -> enviarMensaje());
        campoMensaje.addActionListener(e -> botonEnviar.doClick());

        ventana.setVisible(true);
    }

    private void conectarYEscuchar() {
        try {
            socket = new Socket(host, puerto);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            areaChat.append("✅ Conectado al servidor\n");

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                String msgFinal = mensaje + "\n";
                // Actualizar GUI en hilo de eventos
                SwingUtilities.invokeLater(() -> areaChat.append(msgFinal));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> areaChat.append("❌ Error o conexión cerrada.\n"));
            e.printStackTrace();
        }
    }

    private void enviarMensaje() {
        String mensaje = campoMensaje.getText();
        if (!mensaje.isEmpty() && out != null) {
            out.println(mensaje);
            campoMensaje.setText("");
        }
    }

    public static void main(String[] args) {
        String host = "192.168.18.35"; // o pide con JOptionPane si quieres
        new ClienteChat(host, 12345);
    }
}
    