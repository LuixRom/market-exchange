package com.dbp.proyectobackendmarketexchange.event.usuario;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String frontendBaseUrl;
    private final String resetPasswordPath;

    public PasswordResetRequestedListener(JavaMailSender mailSender,
                                          TemplateEngine templateEngine,
                                          @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                          @Value("${app.frontend.reset-password-path:/reset-password}") String resetPasswordPath) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = frontendBaseUrl;
        this.resetPasswordPath = resetPasswordPath;
    }

    @Async
    @EventListener
    public void manejarPasswordResetRequestedEvent(PasswordResetRequestedEvent event) throws MessagingException {
        Context context = new Context();
        context.setVariable("nombre", event.getUsuario().getFirstname());
        context.setVariable("resetPasswordUrl", buildResetPasswordUrl(event.getResetToken()));

        String contenidoHtml = templateEngine.process("password-reset", context);

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, "utf-8");
        helper.setTo(event.getUsuario().getEmail());
        helper.setSubject("Recupera tu contrasena en MarketExchange");
        helper.setText(contenidoHtml, true);

        mailSender.send(mensaje);
    }

    private String buildResetPasswordUrl(String token) {
        String path = resetPasswordPath.startsWith("/") ? resetPasswordPath : "/" + resetPasswordPath;
        return UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }
}
