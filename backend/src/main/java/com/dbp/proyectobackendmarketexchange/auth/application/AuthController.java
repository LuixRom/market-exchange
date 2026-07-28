package com.dbp.proyectobackendmarketexchange.auth.application;

import com.dbp.proyectobackendmarketexchange.auth.domain.AuthenticationService;
import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;

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
        JwtAuthResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtAuthResponse response = authenticationService.signin(request);
        return ResponseEntity.ok(response);
    }
}
