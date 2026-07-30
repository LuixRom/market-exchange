package com.dbp.proyectobackendmarketexchange.config;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String jwt;
        String userEmail;

        if (!StringUtils.hasText(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extrackUserName(jwt);

            if (StringUtils.hasText(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
                jwtService.validateToken(jwt, userEmail);
            }
        } catch (JWTVerificationException e) {
            // Token ausente/vencido/con firma inválida (p.ej. sobrevivió a un cambio de
            // JWT_SECRET, o el usuario dejó de existir): se trata como request anónima en
            // vez de tirar abajo la respuesta con un 500. Las reglas de SecurityConfig son
            // las que deciden si la ruta requiere autenticación o no.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

