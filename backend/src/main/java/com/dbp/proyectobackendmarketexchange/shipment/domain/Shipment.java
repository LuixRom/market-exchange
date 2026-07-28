package com.dbp.proyectobackendmarketexchange.shipment.domain;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "agreement_id")
    private Agreement agreement;

    @NotBlank(message = "La dirección del iniciador no puede estar vacía")
    private String initiatorAddress;

    @NotBlank(message = "La dirección del receptor no puede estar vacía")
    private String receiveAddress;

    @Future(message = "La fecha de entrega debe estar en el futuro")
    private LocalDateTime deliveryDate;

}
