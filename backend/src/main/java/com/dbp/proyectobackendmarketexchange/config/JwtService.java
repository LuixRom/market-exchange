package com.dbp.proyectobackendmarketexchange.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private final UserDetailsServiceImpl userService;

    public JwtService(UserDetailsServiceImpl userService) {
        this.userService = userService;
    }

    public String extrackUserName(String token) {
        return JWT.decode(token).getSubject();
    }

    public String generateToken(UserDetails data){
        Instant now = Instant.now();
        Instant expiration = now.plus(10, ChronoUnit.HOURS);

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(data.getUsername())
                .withClaim("role", data.getAuthorities().toArray()[0].toString())
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(algorithm);
    }

    public void validateToken(String token, String userEmail) throws AuthenticationException {

        JWT.require(Algorithm.HMAC256(secret)).build().verify(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationFromToken(token, userEmail));
        SecurityContextHolder.setContext(context);
    }

    public UsernamePasswordAuthenticationToken authenticationFromToken(String token, String userEmail) throws AuthenticationException {
        JWT.require(Algorithm.HMAC256(secret)).build().verify(token);

        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userEmail);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }
}
