package com.dbp.proyectobackendmarketexchange.event.usuario;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UsuarioCreadoEvent extends ApplicationEvent {
    private final Usuario usuario;
    private final String verificationToken;

    public UsuarioCreadoEvent(Object source, Usuario usuario) {
        this(source, usuario, null);
    }

    public UsuarioCreadoEvent(Object source, Usuario usuario, String verificationToken) {
        super(source);
        this.usuario = usuario;
        this.verificationToken = verificationToken;
    }
}
