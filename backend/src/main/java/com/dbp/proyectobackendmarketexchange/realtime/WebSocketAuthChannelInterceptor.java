package com.dbp.proyectobackendmarketexchange.realtime;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;

    public WebSocketAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            String userEmail = jwtService.extrackUserName(token);
            UsernamePasswordAuthenticationToken authentication = jwtService.authenticationFromToken(token, userEmail);
            accessor.setUser(authentication);
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }

        if (StringUtils.hasText(authorization) && StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            return authorization.substring(7);
        }

        String token = accessor.getFirstNativeHeader("token");
        if (StringUtils.hasText(token)) {
            return token;
        }

        throw new IllegalArgumentException("Token JWT requerido para conectar al WebSocket");
    }
}
