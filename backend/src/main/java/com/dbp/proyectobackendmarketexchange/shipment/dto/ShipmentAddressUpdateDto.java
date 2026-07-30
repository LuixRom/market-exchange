package com.dbp.proyectobackendmarketexchange.shipment.dto;

import com.dbp.proyectobackendmarketexchange.shipment.domain.ShipmentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShipmentAddressUpdateDto {

    @Size(max = 255, message = "La direccion no puede tener mas de 255 caracteres")
    private String initiatorAddress;

    @Size(max = 255, message = "La direccion no puede tener mas de 255 caracteres")
    private String receiveAddress;

    @Future(message = "La fecha de entrega debe estar en el futuro")
    private LocalDateTime deliveryDate;

    private ShipmentMethod method;
}
