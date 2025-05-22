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

    // Constructor: define host y puerto, y lanza la GUI
    public ClienteChat(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;

        // Crear la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            crearGUI();
            // Conectar al servidor y comenzar a escuchar mensajes en un hilo separado
            new Thread(() -> conectarYEscuchar()).start();
        });
    }

    // Método que construye la interfaz gráfica del cliente
    private void crearGUI() {
        ventana = new JFrame("Cliente Chat");
        ventana.setSize(400, 400);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        areaChat = new JTextArea();
        areaChat.setEditable(false); // Solo lectura para mostrar mensajes
        JScrollPane scroll = new JScrollPane(areaChat);

        campoMensaje = new JTextField(); // Campo para escribir mensajes
        botonEnviar = new JButton("Enviar");

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(campoMensaje, BorderLayout.CENTER);
        panelInferior.add(botonEnviar, BorderLayout.EAST);

        ventana.getContentPane().add(scroll, BorderLayout.CENTER);
        ventana.getContentPane().add(panelInferior, BorderLayout.SOUTH);

        // Eventos para enviar mensaje al hacer clic o presionar Enter
        botonEnviar.addActionListener(_ -> enviarMensaje());
        campoMensaje.addActionListener(_ -> botonEnviar.doClick());

        ventana.setVisible(true); // Mostrar ventana
    }

    // Establece la conexión TCP y escucha mensajes del servidor
    private void conectarYEscuchar() {
        try {
            socket = new Socket(host, puerto); // Conexión al servidor
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            areaChat.append("✅ Conectado al servidor\n");

            // Recibir mensajes del servidor y mostrarlos en el área de texto
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

    // Envía el mensaje escrito al servidor
    private void enviarMensaje() {
        String mensaje = campoMensaje.getText();
        if (!mensaje.isEmpty() && out != null) {
            out.println(mensaje); // Enviar al servidor
            campoMensaje.setText(""); // Limpiar el campo de entrada
        }
    }

    // Método principal: inicia el cliente con IP y puerto definidos
    public static void main(String[] args) {
        String host = "192.168.18.35"; // Cambiar según la IP del servidor
        new ClienteChat(host, 12345);
    }
}
