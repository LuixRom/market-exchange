package com.dbp.proyectobackendmarketexchange.tradeproposal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TradeProposalRequestDto {

    @NotNull(message = "El item ofrecido no puede ser nulo")
    private Long offeredItemId;

    @NotNull(message = "El item solicitado no puede ser nulo")
    private Long requestedItemId;
}
