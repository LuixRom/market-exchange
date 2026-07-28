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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si hay un usuario autenticado.
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // En este caso, el correo es el nombre de usuario.

        // Busca al usuario por su email y rol.
        Usuario usuario = userDetailsService.loadUserByUsername(email);

        // Verifica si el usuario es el propietario del recurso o es administrador.
        return usuario.getId().equals(id) || usuario.getRole().equals(Role.ADMIN);
    }
}
