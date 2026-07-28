package com.dbp.proyectobackendmarketexchange.shipment.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class ShipmentRequestDto {

    @NotNull(message = "La dirección del iniciador no puede ser nula")
    private String initiatorAddress;

    @NotNull(message = "La dirección del receptor no puede ser nula")
    private String receiveAddress;

    @NotNull(message = "La fecha de entrega no puede ser nula")
    private LocalDateTime deliveryDate;

    @NotNull(message = "El acuerdo no puede ser nulo")
    private Long agreementId;
}
