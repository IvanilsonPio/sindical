package com.sindicato;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sindicato.config.DatabaseUrlConverter;

@SpringBootApplication
public class SistemaSindicatoRuralApplication {

    public static void main(String[] args) throws Exception {
        // Abre a porta 8080 imediatamente para satisfazer o healthcheck do Railway
        // enquanto o Spring Boot inicializa em background
        Thread warmupServer = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(8080)) {
                // Responde requisições HTTP com 503 até o Spring assumir a porta
                while (!server.isClosed()) {
                    try {
                        Socket client = server.accept();
                        OutputStream out = client.getOutputStream();
                        String response = "HTTP/1.1 503 Service Unavailable\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n\r\n";
                        out.write(response.getBytes());
                        out.flush();
                        client.close();
                    } catch (IOException ignored) {
                        // Socket fechado quando o Spring assumiu a porta
                        break;
                    }
                }
            } catch (IOException ignored) {
                // Porta já em uso (Spring assumiu) ou erro — ignora
            }
        });
        warmupServer.setDaemon(true);
        warmupServer.start();

        SpringApplication app = new SpringApplication(SistemaSindicatoRuralApplication.class);
        app.addInitializers(new DatabaseUrlConverter());
        app.run(args);
    }

}