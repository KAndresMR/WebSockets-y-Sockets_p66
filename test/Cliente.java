import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class Cliente {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JFrame ventana;
    private JTextArea areaChat;
    private JTextField campoMensaje;
    private JButton botonEnviar;

    public Cliente(String host, int puerto) {
        String nombreUsuario = JOptionPane.showInputDialog("Nombre de usuario:");
        String sala = JOptionPane.showInputDialog("Nombre de la sala:");

        if (nombreUsuario == null || sala == null || nombreUsuario.isEmpty() || sala.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar nombre de usuario y sala.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            crearGUI();
            new Thread(() -> conectarYEscuchar(host, puerto, nombreUsuario, sala)).start();
        });
    }

    private void crearGUI() {
        ventana = new JFrame("Cliente");
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

        botonEnviar.addActionListener(_ -> enviarMensaje());
        campoMensaje.addActionListener(_ -> botonEnviar.doClick());

        ventana.setVisible(true);
    }

    private void conectarYEscuchar(String host, int puerto, String nombreUsuario, String sala) {
        try {
            socket = new Socket(host, puerto);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Esperar solicitud del servidor y responder
            in.readLine(); // "Ingrese su nombre de usuario:"
            out.println(nombreUsuario);
            in.readLine(); // "Ingrese el nombre de la sala..."
            out.println(sala);

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                String msgFinal = mensaje + "\n";
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
        String host = "192.168.18.35";//JOptionPane.showInputDialog("IP del servidor:");
        new Cliente(host, 12345);
    }
}
