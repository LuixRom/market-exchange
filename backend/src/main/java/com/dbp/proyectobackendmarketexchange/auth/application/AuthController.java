package com.dbp.proyectobackendmarketexchange.auth.application;

import com.dbp.proyectobackendmarketexchange.auth.domain.AuthenticationService;
import com.dbp.proyectobackendmarketexchange.auth.dto.ForgotPasswordRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.MessageResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.ResetPasswordRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.TokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody TokenRequest request) {
        authenticationService.verifyEmail(request.getToken());
        return ResponseEntity.ok(new MessageResponse("Correo verificado correctamente", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authenticationService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("Si el correo existe, se genero un token de recuperacion", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contrasena actualizada correctamente", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@Valid @RequestBody TokenRequest request) {
        return ResponseEntity.ok(authenticationService.refresh(request.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody TokenRequest request) {
        authenticationService.logout(request.getToken());
        return ResponseEntity.ok(new MessageResponse("Sesion cerrada correctamente", null));
    }
}
