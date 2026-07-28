package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "trade_proposal_id")
    private TradeProposal tradeProposal;

    @NotBlank(message = "La dirección del iniciador no puede estar vacía")
    private String initiatorAddress;

    @NotBlank(message = "La dirección del receptor no puede estar vacía")
    private String receiveAddress;

    @Future(message = "La fecha de entrega debe estar en el futuro")
    private LocalDateTime deliveryDate;

    @NotNull(message = "El estado del envío no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.PENDING;

}
