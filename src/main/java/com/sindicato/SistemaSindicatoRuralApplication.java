package com.sindicato;

import com.sindicato.config.DatabaseUrlConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaSindicatoRuralApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SistemaSindicatoRuralApplication.class);
        app.addInitializers(new DatabaseUrlConverter());
        app.run(args);
    }

}