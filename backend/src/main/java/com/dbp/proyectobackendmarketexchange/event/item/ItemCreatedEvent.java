package com.dbp.proyectobackendmarketexchange.event.item;

import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import lombok.Getter;

import org.springframework.context.ApplicationEvent;

@Getter
public class ItemCreatedEvent extends ApplicationEvent {

    private final Item item;

    public ItemCreatedEvent(Object source, Item item) {
        super(source);
        this.item = item;
    }
}
