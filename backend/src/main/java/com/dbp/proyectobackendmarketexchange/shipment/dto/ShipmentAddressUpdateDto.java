package com.dbp.proyectobackendmarketexchange.shipment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Cada campo es opcional: solo se aplica el que venga, y cada uno tiene su propia
 * autorización (initiatorAddress -> proposer, receiveAddress -> receiver). No incluye
 * status a propósito -las transiciones de estado van por las rutas de acción dedicadas
 * (prepare/ship/deliver/cancel), no por este endpoint genérico-.
 */
@Getter
@Setter
public class ShipmentAddressUpdateDto {

    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String initiatorAddress;

    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String receiveAddress;
}
