package com.dbp.proyectobackendmarketexchange.event.tradeproposal;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TradeProposalCreatedEvent extends ApplicationEvent {

    private final transient TradeProposal tradeProposal;

    public TradeProposalCreatedEvent(Object source, TradeProposal tradeProposal) {
        super(source);
        this.tradeProposal = tradeProposal;
    }
}
