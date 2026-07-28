package com.dbp.proyectobackendmarketexchange.auth.utils;


import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UserDetailsServiceImpl;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtils {
    private final UserDetailsServiceImpl userDetailsService;

    public AuthorizationUtils(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public boolean isAdminOrResourceOwner(Long id) {
        Usuario usuario = currentUsuario();
        if (usuario == null) {
            return false;
        }
        return usuario.getId().equals(id) || usuario.getRole().equals(Role.ADMIN);
    }

    /**
     * Igual que {@link #isAdminOrResourceOwner(Long)} pero para recursos con dos partes
     * (p.ej. un Shipment, dueño de un lado y de otro) — resuelve el usuario actual una
     * sola vez en vez de llamar dos veces isAdminOrResourceOwner con un OR.
     */
    public boolean isAdminOrResourceOwner(Long id1, Long id2) {
        Usuario usuario = currentUsuario();
        if (usuario == null) {
            return false;
        }
        return usuario.getId().equals(id1) || usuario.getId().equals(id2) || usuario.getRole().equals(Role.ADMIN);
    }

    /**
     * A diferencia de isAdminOrResourceOwner, no hay "dueño" que bypassear: solo ADMIN.
     * Útil para acciones puramente administrativas (p.ej. borrar un Rating ya emitido).
     */
    public boolean isAdmin() {
        Usuario usuario = currentUsuario();
        return usuario != null && usuario.getRole().equals(Role.ADMIN);
    }

    private Usuario currentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si hay un usuario autenticado.
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // En este caso, el correo es el nombre de usuario.

        // Busca al usuario por su email y rol.
        return userDetailsService.loadUserByUsername(email);
    }
}
