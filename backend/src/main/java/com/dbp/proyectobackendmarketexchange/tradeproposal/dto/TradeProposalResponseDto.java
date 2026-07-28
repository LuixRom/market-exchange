package com.dbp.proyectobackendmarketexchange.tradeproposal.dto;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeProposalResponseDto {
    private Long id;
    private TradeStatus status;

    private Long offeredItemId;
    private String offeredItemName;

    private Long requestedItemId;
    private String requestedItemName;

    private Long proposerId;
    private String proposerEmail;

    private Long receiverId;
    private String receiverEmail;
}
