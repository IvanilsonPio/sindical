package com.sindicato.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Envia emails via Resend API (HTTPS) para contornar bloqueio de porta SMTP no Railway.
 * Configure RESEND_API_KEY e RESEND_FROM nas variáveis de ambiente.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from:noreply@sindicato.com}")
    private String remetente;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public void enviarRecuperacaoSenha(String destinatario, String nomeUsuario, String token) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            logger.warn("RESEND_API_KEY não configurada. E-mail não enviado para: {}", destinatario);
            return;
        }

        String link = frontendUrl + "/recuperar-senha/redefinir?token=" + token;
        String corpo = "Olá, " + nomeUsuario + "!\n\n" +
                "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n" +
                "Clique no link abaixo para criar uma nova senha (válido por 1 hora):\n\n" +
                link + "\n\n" +
                "Se você não solicitou a recuperação de senha, ignore este e-mail.\n\n" +
                "Atenciosamente,\nSistema Sindicato Rural";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            Map<String, Object> body = Map.of(
                    "from", remetente,
                    "to", new String[]{destinatario},
                    "subject", "Recuperação de Senha — Sistema Sindicato Rural",
                    "text", corpo
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            new RestTemplate().postForObject(RESEND_API_URL, request, Map.class);

            logger.info("E-mail de recuperação enviado via Resend para: {}", destinatario);

        } catch (Exception e) {
            logger.error("Erro ao enviar e-mail via Resend para {}: {}", destinatario, e.getMessage());
            throw new com.sindicato.exception.BusinessException(
                    "EMAIL_SEND_ERROR",
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde."
            );
        }
    }
}
