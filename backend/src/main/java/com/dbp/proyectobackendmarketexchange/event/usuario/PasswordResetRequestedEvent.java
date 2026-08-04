package com.dbp.proyectobackendmarketexchange.event.usuario;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import org.springframework.context.ApplicationEvent;

public class PasswordResetRequestedEvent extends ApplicationEvent {
    private final Usuario usuario;
    private final String resetToken;

    public PasswordResetRequestedEvent(Object source, Usuario usuario, String resetToken) {
        super(source);
        this.usuario = usuario;
        this.resetToken = resetToken;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getResetToken() {
        return resetToken;
    }
}
