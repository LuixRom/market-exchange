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
public class UsuarioCreadoListener {
    private static final Logger log = LoggerFactory.getLogger(UsuarioCreadoListener.class);
    private static final String PATH_DELIMITER = "/";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String frontendBaseUrl;
    private final String verifyEmailPath;
    private final String fromAddress;
    private final String fromName;

    public UsuarioCreadoListener(JavaMailSender mailSender,
                                 TemplateEngine templateEngine,
                                 @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                 @Value("${app.frontend.verify-email-path:/verify-email}") String verifyEmailPath,
                                 @Value("${app.mail.from-address:}") String fromAddress,
                                 @Value("${app.mail.from-name:MarketExchange}") String fromName) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = frontendBaseUrl;
        this.verifyEmailPath = verifyEmailPath;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Async
    @EventListener
    public void manejarUsuarioCreadoEvent(UsuarioCreadoEvent event) {
        String email = event.getUsuario().getEmail();
        String nombre = event.getUsuario().getFirstname();
        boolean requiresVerification = event.getVerificationToken() != null && !event.getVerificationToken().isBlank();
        String subject = requiresVerification ? "Verifica tu correo en MarketExchange" : "Bienvenido a MarketExchange";

        Context context = new Context();
        context.setVariable("nombre", nombre);
        if (requiresVerification) {
            context.setVariable("verificationUrl", buildVerificationUrl(event.getVerificationToken()));
        }

        String contenidoHtml = templateEngine.process(requiresVerification ? "email-verification" : "welcome-email", context);
        String contenidoTexto = requiresVerification
                ? "Hola " + nombre + ",\n\nVerifica tu correo en MarketExchange ingresando al siguiente enlace:\n" + buildVerificationUrl(event.getVerificationToken())
                : "Hola " + nombre + ",\n\nBienvenido a MarketExchange.";

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "utf-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(contenidoTexto, contenidoHtml);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress, fromName);
            }

            mailSender.send(mensaje);
        } catch (Exception ex) {
            log.error("No se pudo enviar el correo de {} a {}: {}",
                    requiresVerification ? "verificacion" : "bienvenida", email, ex.getMessage(), ex);
        }
    }

    private String buildVerificationUrl(String token) {
        String path = verifyEmailPath.startsWith(PATH_DELIMITER) ? verifyEmailPath : PATH_DELIMITER + verifyEmailPath;
        return UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(path)
                .queryParam("token", token)
                .toUriString();
    }
}
