package com.sindicato.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encoded = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("SENHA CRIPTOGRAFADA (BCrypt)");
        System.out.println("========================================");
        System.out.println("Senha original: " + password);
        System.out.println("Senha criptografada: " + encoded);
        System.out.println();
        System.out.println("========================================");
        System.out.println("SQL PARA INSERIR USUÁRIO ADMIN");
        System.out.println("========================================");
        System.out.println("INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em)");
        System.out.println("VALUES ('admin', '" + encoded + "', 'Administrador', 'ATIVO', NOW(), NOW());");
        System.out.println();
        System.out.println("========================================");
        System.out.println("VERIFICAR SENHA");
        System.out.println("========================================");
        boolean matches = encoder.matches(password, encoded);
        System.out.println("Senha confere: " + matches);
    }
}
