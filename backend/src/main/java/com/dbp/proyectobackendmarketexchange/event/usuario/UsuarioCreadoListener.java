package com.dbp.proyectobackendmarketexchange.event.usuario;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;




import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UsuarioCreadoListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public UsuarioCreadoListener(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @EventListener
    public void manejarUsuarioCreadoEvent(UsuarioCreadoEvent event) throws MessagingException {
        // Obtener el usuario desde el evento
        String email = event.getUsuario().getEmail();
        String nombre = event.getUsuario().getFirstname();

        // Preparar el contexto para Thymeleaf
        Context context = new Context();
        context.setVariable("nombre", nombre);

        // Procesar la plantilla HTML usando Thymeleaf
        String contenidoHtml = templateEngine.process("welcome-email", context);

        // Crear el mensaje MIME para enviar HTML
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, "utf-8");

        // Configurar los detalles del correo
        helper.setTo(email);
        helper.setSubject("¡Bienvenido a MarketExchange!");
        helper.setText(contenidoHtml, true);

        // Enviar el correo
        mailSender.send(mensaje);
    }
}