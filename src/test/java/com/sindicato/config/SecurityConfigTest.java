package com.sindicato.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Testes para a configuração de segurança.
 * Valida o Requisito 1.4: O Sistema DEVE criptografar as senhas dos administradores usando algoritmos seguros
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Test
    void deveConfigurarPasswordEncoderComBCrypt() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }
    
    @Test
    void deveCriptografarSenhaCorretamente() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String senhaOriginal = "senha123";
        
        String senhaCriptografada = passwordEncoder.encode(senhaOriginal);
        
        assertThat(senhaCriptografada).isNotNull();
        assertThat(senhaCriptografada).isNotEqualTo(senhaOriginal);
        assertThat(senhaCriptografada).startsWith("$2a$"); // BCrypt hash prefix
    }
    
    @Test
    void deveGerarHashesDiferentesParaMesmaSenha() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String senhaOriginal = "senha123";
        
        String hash1 = passwordEncoder.encode(senhaOriginal);
        String hash2 = passwordEncoder.encode(senhaOriginal);
        
        // BCrypt gera hashes diferentes devido ao salt aleatório
        assertThat(hash1).isNotEqualTo(hash2);
        
        // Mas ambos devem validar corretamente
        assertThat(passwordEncoder.matches(senhaOriginal, hash1)).isTrue();
        assertThat(passwordEncoder.matches(senhaOriginal, hash2)).isTrue();
    }
    
    @Test
    void deveValidarSenhaCorretamente() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String senhaOriginal = "senha123";
        String senhaCriptografada = passwordEncoder.encode(senhaOriginal);
        
        boolean senhaCorreta = passwordEncoder.matches(senhaOriginal, senhaCriptografada);
        boolean senhaIncorreta = passwordEncoder.matches("senhaErrada", senhaCriptografada);
        
        assertThat(senhaCorreta).isTrue();
        assertThat(senhaIncorreta).isFalse();
    }
    
    @Test
    void deveRejeitarSenhaIncorreta() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String senhaOriginal = "senha123";
        String senhaCriptografada = passwordEncoder.encode(senhaOriginal);
        
        boolean resultado = passwordEncoder.matches("outraSenha", senhaCriptografada);
        
        assertThat(resultado).isFalse();
    }
    
    @Test
    void deveSuportarSenhasComCaracteresEspeciais() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService);
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String senhaComplexa = "S3nh@!C0mpl3x@#$%";
        
        String senhaCriptografada = passwordEncoder.encode(senhaComplexa);
        
        assertThat(passwordEncoder.matches(senhaComplexa, senhaCriptografada)).isTrue();
    }
}
