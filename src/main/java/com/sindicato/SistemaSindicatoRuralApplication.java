package com.sindicato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import com.sindicato.config.DatabaseUrlConverter;

@SpringBootApplication
public class SistemaSindicatoRuralApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SistemaSindicatoRuralApplication.class);
        app.addInitializers(new DatabaseUrlConverter());
        app.run(args);
    }

    // Força inicialização eager do actuator health ao iniciar
    @Bean
    public ApplicationListener<ApplicationReadyEvent> onReady() {
        return event -> System.out.println("=== Application ready on port "
                + event.getApplicationContext().getEnvironment().getProperty("server.port", "8080")
                + " ===");
    }
}
