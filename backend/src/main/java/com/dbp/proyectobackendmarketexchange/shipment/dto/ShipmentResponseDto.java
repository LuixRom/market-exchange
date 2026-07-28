package com.dbp.proyectobackendmarketexchange.shipment.dto;

import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShipmentResponseDto {

    private Long id;
    private String initiatorAddress;
    private String receiveAddress;
    private LocalDateTime deliveryDate;
    private Long tradeProposalId;
    private ShipmentStatus status;
    private String trackingCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime preparedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
}
