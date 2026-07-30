package com.dbp.proyectobackendmarketexchange.chat.infrastructure;

import com.dbp.proyectobackendmarketexchange.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByTradeProposalIdOrderByCreatedAtAsc(Long tradeProposalId);
}
