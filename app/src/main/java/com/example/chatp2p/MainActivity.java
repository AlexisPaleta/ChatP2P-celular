package com.example.chatp2p;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mensajeInput;
    private Button enviarButton;
    private TextView mensajeView;
    private StringBuilder mensajeHistorial;
    private ScrollView mensajeScrollView;
    private static final int PUERTO = 12345;
    private static final String nombreDispositivo = "Celular: ";

    private static List<String> ipsDispositivos = Arrays.asList(
            "192.168.137.1",
            "192.168.137.162"// Cambia las IPs por las de los dispositivos en la red ad-hoc
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mensajeInput = findViewById(R.id.mensajeInput);
        enviarButton = findViewById(R.id.enviarButton);
        mensajeView = findViewById(R.id.mensajeView);
        mensajeScrollView = findViewById(R.id.mensajeScrollView);
        mensajeHistorial = new StringBuilder();

        // Configura el botón para enviar mensajes
        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = mensajeInput.getText().toString();
                enviarMensaje(mensaje);
            }
        });

        // Ejecuta el servidor en un hilo separado para recibir mensajes
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(12345); // Puerto para recibir mensajes
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        BufferedReader inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        final String mensaje = inStream.readLine();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Acumula los mensajes y actualiza el TextView
                                mensajeHistorial.append(mensaje).append("\n");
                                mensajeView.setText(mensajeHistorial.toString());
                                // Desplaza el ScrollView hacia el final
                                mensajeScrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mensajeScrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                        });
                        clientSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void enviarMensaje(final String mensaje) {
        // Este código se ejecutará en un hilo separado
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (String ip : ipsDispositivos) {
                    try (Socket socket = new Socket(ip, PUERTO)) {
                        PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                        salida.println(nombreDispositivo + mensaje);
                    } catch (IOException e) {
                        System.out.println("No se pudo conectar con " + ip);
                    }
                }
            }
        }).start();
    }
}



