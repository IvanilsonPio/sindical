package com.sindicato.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * EMAIL DISABLED: Email sending disabled for Railway deployment.
 * Re-enable when SMTP is configured and JavaMailSender is available.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // EMAIL_DISABLED: JavaMailSender dependency removed to prevent startup connection attempts.

    public void enviarRecuperacaoSenha(String destinatario, String nomeUsuario, String token) {
        // EMAIL_DISABLED: email sending is currently disabled.
        logger.warn("Email sending is disabled. Would send password recovery to: {}", destinatario);
    }
}
