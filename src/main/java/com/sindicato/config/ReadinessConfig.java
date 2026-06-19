package com.sindicato.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Registra um servlet simples em /healthz que responde 200 imediatamente
 * assim que o Undertow abre a porta, antes mesmo do Spring MVC inicializar.
 * Usado pelo Railway healthcheck durante o startup.
 */
@Configuration
public class ReadinessConfig {

    @Bean
    public ServletRegistrationBean<HttpServlet> healthzServlet() {
        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\":\"starting\"}");
            }
        };
        return new ServletRegistrationBean<>(servlet, "/healthz");
    }
}
