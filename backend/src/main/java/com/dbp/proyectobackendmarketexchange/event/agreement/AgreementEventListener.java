package com.dbp.proyectobackendmarketexchange.event.agreement;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Component
public class AgreementEventListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public AgreementEventListener(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @EventListener
    public void onAgreementCreated(AgreementCreadoEvent event) throws MessagingException {
        Agreement agreement = event.getAgreement();


        sendEmail(agreement.getRecipient().getEmail(),
                "Solicitud de Intercambio Recibida",
                "agreement-request-received", agreement);


        sendEmail(agreement.getInitiator().getEmail(),
                "Solicitud de Intercambio Enviada",
                "agreement-request-sent", agreement);
    }

    @Async
    @EventListener
    public void onAgreementAccepted(AgreementAceptadoEvent event) throws MessagingException {
        Agreement agreement = event.getAgreement();


        sendEmail(agreement.getInitiator().getEmail(),
                "Solicitud de Intercambio Aceptada",
                "agreement-accepted-notification", agreement);


        sendEmail(agreement.getRecipient().getEmail(),
                "Has aceptado la solicitud de intercambio",
                "agreement-accepted-confirmation", agreement);
    }

    @Async
    @EventListener
    public void onAgreementRejected(AgreementRechazadoEvent event) throws MessagingException {
        Agreement agreement = event.getAgreement();


        sendEmail(agreement.getInitiator().getEmail(),
                "Solicitud de Intercambio Rechazada",
                "agreement-rejected-notification", agreement);


        sendEmail(agreement.getRecipient().getEmail(),
                "Has rechazado la solicitud de intercambio",
                "agreement-rejected-confirmation", agreement);
    }

    private void sendEmail(String recipientEmail, String subject, String templateName, Agreement agreement) throws MessagingException {
        // Contexto para Thymeleaf
        Context context = new Context();
        context.setVariable("initiatorEmail", agreement.getInitiator().getEmail());
        context.setVariable("recipientEmail", agreement.getRecipient().getEmail());
        context.setVariable("itemIniName", agreement.getItem_ini().getName());
        context.setVariable("itemFinName", agreement.getItem_fin().getName());

        // Procesar la plantilla HTML con Thymeleaf
        String htmlContent = templateEngine.process(templateName, context);

        // Crear el mensaje MIME
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}