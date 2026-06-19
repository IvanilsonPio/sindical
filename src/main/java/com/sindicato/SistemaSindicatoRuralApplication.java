package com.sindicato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sindicato.config.DatabaseUrlConverter;

@SpringBootApplication
public class SistemaSindicatoRuralApplication {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.err.println("=== SHUTDOWN HOOK: JVM sendo encerrada ===")
        ));

        SpringApplication app = new SpringApplication(SistemaSindicatoRuralApplication.class);
        app.addInitializers(new DatabaseUrlConverter());
        app.run(args);
    }

}
