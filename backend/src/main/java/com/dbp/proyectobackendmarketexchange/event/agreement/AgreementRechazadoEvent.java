package com.dbp.proyectobackendmarketexchange.event.agreement;

import com.dbp.proyectobackendmarketexchange.agreement.domain.Agreement;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AgreementRechazadoEvent extends ApplicationEvent {

    private final Agreement agreement;

    public AgreementRechazadoEvent(Object source, Agreement agreement) {
        super(source);
        this.agreement = agreement;
    }
}
