package com.dbp.proyectobackendmarketexchange.usuario.domain;

import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository
                .findByEmail(EmailNormalizer.normalize(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + username));
    }


    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Usuario user = usuarioRepository
                    .findByEmail(EmailNormalizer.normalize(username))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return (UserDetails) user;
        };
    }

}
