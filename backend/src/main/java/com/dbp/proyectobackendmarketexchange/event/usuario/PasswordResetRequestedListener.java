package com.dbp.proyectobackendmarketexchange.event.usuario;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class PasswordResetRequestedListener {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetRequestedListener.class);
    private static final String PATH_DELIMITER = "/";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String frontendBaseUrl;
    private final String resetPasswordPath;
    private final String fromAddress;
    private final String fromName;

    public PasswordResetRequestedListener(JavaMailSender mailSender,
                                          TemplateEngine templateEngine,
                                          @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                          @Value("${app.frontend.reset-password-path:/reset-password}") String resetPasswordPath,
                                          @Value("${app.mail.from-address:}") String fromAddress,
                                          @Value("${app.mail.from-name:MarketExchange}") String fromName) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPasswordPath = resetPasswordPath;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Async
    @EventListener
    public void manejarPasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        String email = event.getUsuario().getEmail();
        String resetPasswordUrl = buildResetPasswordUrl(event.getResetToken());

        Context context = new Context();
        context.setVariable("nombre", event.getUsuario().getFirstname());
        context.setVariable("resetPasswordUrl", resetPasswordUrl);

        String contenidoHtml = templateEngine.process("password-reset", context);
        String contenidoTexto = "Hola " + event.getUsuario().getFirstname()
                + ",\n\nRecupera tu contrasena en MarketExchange ingresando al siguiente enlace:\n" + resetPasswordUrl;

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "utf-8");
            helper.setTo(email);
            helper.setSubject("Recupera tu contrasena en MarketExchange");
            helper.setText(contenidoTexto, contenidoHtml);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress, fromName);
            }

            mailSender.send(mensaje);
        } catch (Exception ex) {
            log.error("No se pudo enviar el correo de restablecimiento de contrasena a {}: {}", email, ex.getMessage(), ex);
        }
    }

    private String buildResetPasswordUrl(String token) {
        String path = resetPasswordPath.startsWith(PATH_DELIMITER) ? resetPasswordPath : PATH_DELIMITER + resetPasswordPath;
        return UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }
}
