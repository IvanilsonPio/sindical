package com.sindicato.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Converte DATABASE_URL no formato postgres:// (injetado pelo Fly.io)
 * para o formato jdbc:postgresql:// exigido pelo driver JDBC.
 *
 * Exemplo:
 *   postgres://user:pass@host:5432/db?sslmode=disable
 *   -> jdbc:postgresql://host:5432/db?sslmode=disable
 *   com spring.datasource.username=user e spring.datasource.password=pass
 */
public class DatabaseUrlConverter implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String databaseUrl = env.getProperty("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.startsWith("jdbc:")) {
            return; // Já está no formato correto ou não existe
        }

        try {
            // postgres://username:password@host:port/database?params
            String withoutScheme = databaseUrl.replaceFirst("^postgres(ql)?://", "");

            String userInfo = withoutScheme.substring(0, withoutScheme.indexOf('@'));
            String rest = withoutScheme.substring(withoutScheme.indexOf('@') + 1);

            String username = userInfo.contains(":") ? userInfo.substring(0, userInfo.indexOf(':')) : userInfo;
            String password = userInfo.contains(":") ? userInfo.substring(userInfo.indexOf(':') + 1) : "";

            String jdbcUrl = "jdbc:postgresql://" + rest;
            // Garante sslmode=disable para rede interna do Fly.io
            if (!jdbcUrl.contains("sslmode")) {
                jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=disable";
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", username);
            props.put("spring.datasource.password", password);

            env.getPropertySources().addFirst(
                new MapPropertySource("flyDatabaseUrl", props)
            );

        } catch (Exception e) {
            throw new IllegalStateException("Falha ao converter DATABASE_URL: " + databaseUrl, e);
        }
    }
}
