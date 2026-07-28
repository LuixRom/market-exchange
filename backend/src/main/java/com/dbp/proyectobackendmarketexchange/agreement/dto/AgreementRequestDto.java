package com.dbp.proyectobackendmarketexchange.agreement.dto;

import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import lombok.Getter;
import lombok.Setter;


import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class AgreementRequestDto {

    private State state;

    @NotNull(message = "El objeto ofrecido por el iniciador no puede ser nulo")
    private Long itemIniId;

    @NotNull(message = "El objeto ofrecido por el receptor no puede ser nulo")
    private Long itemFinId;

    @NotNull(message = "El usuario iniciador no puede ser nulo")
    private Long usuarioIniId;

    @NotNull(message = "El usuario receptor no puede ser nulo")
    private Long usuarioFinId;
}