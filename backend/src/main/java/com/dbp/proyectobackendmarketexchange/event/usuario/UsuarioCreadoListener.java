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
public class UsuarioCreadoListener {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String frontendBaseUrl;
    private final String verifyEmailPath;

    public UsuarioCreadoListener(JavaMailSender mailSender,
                                 TemplateEngine templateEngine,
                                 @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                 @Value("${app.frontend.verify-email-path:/verify-email}") String verifyEmailPath) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = frontendBaseUrl;
        this.verifyEmailPath = verifyEmailPath;
    }

    @Async
    @EventListener
    public void manejarUsuarioCreadoEvent(UsuarioCreadoEvent event) throws MessagingException {
        String email = event.getUsuario().getEmail();
        String nombre = event.getUsuario().getFirstname();
        boolean requiresVerification = event.getVerificationToken() != null && !event.getVerificationToken().isBlank();

        Context context = new Context();
        context.setVariable("nombre", nombre);
        if (requiresVerification) {
            context.setVariable("verificationUrl", buildVerificationUrl(event.getVerificationToken()));
        }

        String contenidoHtml = templateEngine.process(requiresVerification ? "email-verification" : "welcome-email", context);

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, "utf-8");
        helper.setTo(email);
        helper.setSubject(requiresVerification ? "Verifica tu correo en MarketExchange" : "Bienvenido a MarketExchange");
        helper.setText(contenidoHtml, true);

        mailSender.send(mensaje);
    }

    private String buildVerificationUrl(String token) {
        String path = verifyEmailPath.startsWith("/") ? verifyEmailPath : "/" + verifyEmailPath;
        return UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }
}
