package com.sindicato.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sindicato.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuração de segurança do sistema.
 * Define beans relacionados à segurança, incluindo o encoder de senhas, gerenciador de autenticação,
 * proteção de endpoints, controle de sessão e CORS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configura o encoder de senhas usando BCrypt.
     * BCrypt é um algoritmo de hash seguro que inclui salt automático
     * e é resistente a ataques de força bruta.
     *
     * @return instância de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cria o filtro de autenticação JWT como bean.
     *
     * @param jwtUtil utilitário JWT
     * @param userDetailsService serviço de detalhes do usuário
     * @return instância de JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            com.sindicato.util.JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    /**
     * Configura o provedor de autenticação usando UserDetailsService e PasswordEncoder.
     *
     * @return instância de DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expõe o AuthenticationManager como bean para uso no AuthService.
     *
     * @param config configuração de autenticação
     * @return instância de AuthenticationManager
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura a cadeia de filtros de segurança com proteção de endpoints,
     * controle de sessão stateless e integração com JWT.
     *
     * @param http configuração de segurança HTTP
     * @param jwtAuthenticationFilter filtro de autenticação JWT
     * @return cadeia de filtros de segurança configurada
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Desabilita CSRF pois usamos JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configura CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configura autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (não requerem autenticação)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health/**",
                                "/error"
                        ).permitAll()
                        
                        // Gestão de usuários - apenas ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                
                // Configura gerenciamento de sessão como STATELESS
                // Não cria sessão HTTP, usa apenas JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configura tratamento de exceções de autenticação
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                
                // Adiciona provedor de autenticação
                .authenticationProvider(authenticationProvider())
                
                // Adiciona filtro JWT antes do filtro de autenticação padrão
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura o ponto de entrada de autenticação para retornar 401 Unauthorized
     * quando o acesso é negado por falta de autenticação.
     *
     * @return instância de AuthenticationEntryPoint
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        };
    }

    /**
     * Configura CORS (Cross-Origin Resource Sharing) para permitir requisições
     * do frontend Angular em diferentes origens.
     *
     * @return configuração de CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite origens específicas (ajustar conforme ambiente)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",  // Angular dev server
                "http://localhost:8080"   // Produção local
        ));
        
        // Permite métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Permite headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));
        
        // Expõe headers na resposta
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Permite credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Tempo de cache da configuração CORS (em segundos)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
