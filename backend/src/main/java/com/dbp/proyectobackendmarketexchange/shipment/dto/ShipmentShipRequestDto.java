package com.dbp.proyectobackendmarketexchange.shipment.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentShipRequestDto {

    @Size(max = 100, message = "El código de seguimiento no puede tener más de 100 caracteres")
    private String trackingCode;
}
