package com.sindicato.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Converte DATABASE_URL (formato Railway/Heroku: postgresql://user:pass@host:port/db)
 * para as propriedades Spring datasource separadas.
 */
public class DatabaseUrlConfig implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            // Normaliza o schema para que URI consiga parsear
            URI uri = new URI(databaseUrl.replace("postgresql://", "http://")
                                         .replace("postgres://", "http://"));

            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String path = uri.getPath(); // /dbname
            String userInfo = uri.getUserInfo(); // user:pass

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + "?sslmode=require";
            String username = "";
            String password = "";

            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                password = parts[1];
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", username);
            props.put("spring.datasource.password", password);

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("databaseUrlConfig", props));

        } catch (Exception e) {
            throw new IllegalStateException("Falha ao parsear DATABASE_URL: " + databaseUrl, e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
