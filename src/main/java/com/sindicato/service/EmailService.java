package com.sindicato.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@sindicato.com}")
    private String remetente;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarRecuperacaoSenha(String destinatario, String nomeUsuario, String token) {
        String link = frontendUrl + "/recuperar-senha/redefinir?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(destinatario);
        message.setSubject("Recuperação de Senha — Sistema Sindicato Rural");
        message.setText(
            "Olá, " + nomeUsuario + "!\n\n" +
            "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n" +
            "Clique no link abaixo para criar uma nova senha (válido por 1 hora):\n\n" +
            link + "\n\n" +
            "Se você não solicitou a recuperação de senha, ignore este e-mail.\n\n" +
            "Atenciosamente,\nSistema Sindicato Rural"
        );

        try {
            mailSender.send(message);
            logger.info("E-mail de recuperação enviado para: {}", destinatario);
        } catch (Exception e) {
            logger.error("Erro ao enviar e-mail de recuperação para {}: {}", destinatario, e.getMessage());
            throw new com.sindicato.exception.BusinessException(
                "EMAIL_SEND_ERROR",
                "Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde."
            );
        }
    }
}
