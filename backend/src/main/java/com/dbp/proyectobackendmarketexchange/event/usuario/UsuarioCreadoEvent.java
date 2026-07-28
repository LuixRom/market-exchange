package com.dbp.proyectobackendmarketexchange.event.usuario;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import lombok.Getter;

import org.springframework.context.ApplicationEvent;





@Getter
public class UsuarioCreadoEvent extends ApplicationEvent {

    private final Usuario usuario;

    public UsuarioCreadoEvent(Object source, Usuario usuario) {
        super(source);
        this.usuario = usuario;
    }
}