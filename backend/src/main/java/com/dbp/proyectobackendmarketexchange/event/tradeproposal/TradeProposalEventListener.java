package com.dbp.proyectobackendmarketexchange.event.tradeproposal;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Component
public class TradeProposalEventListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public TradeProposalEventListener(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTradeProposalCreated(TradeProposalCreatedEvent event) throws MessagingException {
        TradeProposal tradeProposal = event.getTradeProposal();

        sendEmail(tradeProposal.getReceiver().getEmail(),
                "Solicitud de Intercambio Recibida",
                "trade-proposal-request-received", tradeProposal);

        sendEmail(tradeProposal.getProposer().getEmail(),
                "Solicitud de Intercambio Enviada",
                "trade-proposal-request-sent", tradeProposal);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTradeProposalAccepted(TradeProposalAcceptedEvent event) throws MessagingException {
        TradeProposal tradeProposal = event.getTradeProposal();

        sendEmail(tradeProposal.getProposer().getEmail(),
                "Solicitud de Intercambio Aceptada",
                "trade-proposal-accepted-notification", tradeProposal);

        sendEmail(tradeProposal.getReceiver().getEmail(),
                "Has aceptado la solicitud de intercambio",
                "trade-proposal-accepted-confirmation", tradeProposal);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTradeProposalRejected(TradeProposalRejectedEvent event) throws MessagingException {
        TradeProposal tradeProposal = event.getTradeProposal();

        sendEmail(tradeProposal.getProposer().getEmail(),
                "Solicitud de Intercambio Rechazada",
                "trade-proposal-rejected-notification", tradeProposal);

        sendEmail(tradeProposal.getReceiver().getEmail(),
                "Has rechazado la solicitud de intercambio",
                "trade-proposal-rejected-confirmation", tradeProposal);
    }

    private void sendEmail(String recipientEmail, String subject, String templateName, TradeProposal tradeProposal) throws MessagingException {
        // Contexto para Thymeleaf
        Context context = new Context();
        context.setVariable("proposerEmail", tradeProposal.getProposer().getEmail());
        context.setVariable("receiverEmail", tradeProposal.getReceiver().getEmail());
        context.setVariable("offeredItemName", tradeProposal.getOfferedItem().getName());
        context.setVariable("requestedItemName", tradeProposal.getRequestedItem().getName());

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
