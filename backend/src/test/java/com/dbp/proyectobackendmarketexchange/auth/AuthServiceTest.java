package com.dbp.proyectobackendmarketexchange.auth;

import com.dbp.proyectobackendmarketexchange.auth.domain.AuthenticationService;
import com.dbp.proyectobackendmarketexchange.auth.domain.AccountToken;
import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;
import com.dbp.proyectobackendmarketexchange.auth.exception.UserAlreadyExistException;
import com.dbp.proyectobackendmarketexchange.auth.infrastructure.AccountTokenRepository;
import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private AccountTokenRepository accountTokenRepository;

    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioService usuarioService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(
                userRepository,
                accountTokenRepository,
                jwtService,
                passwordEncoder,
                eventPublisher,
                Clock.systemDefaultZone(),
                24,
                30,
                14
        );
        when(accountTokenRepository.save(any(AccountToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSignInSuccess() {
        // Preparar datos
        Usuario user = new Usuario();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setEmailVerified(true);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        // Ejecutar el método
        JwtAuthResponse response = authenticationService.signin(loginRequest);

        // Verificar el resultado
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(userRepository, times(1)).findByEmail(loginRequest.getUsername());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void testSignInUserNotFound() {
        // Preparar datos
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción de BadCredentialsException
        assertThrows(BadCredentialsException.class, () -> authenticationService.signin(loginRequest));

        verify(userRepository, times(1)).findByEmail(loginRequest.getUsername());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    void testSignInInvalidPassword() {
        // Preparar datos
        Usuario user = new Usuario();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setEmailVerified(true);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Verificar que se lanza la excepción de BadCredentialsException
        assertThrows(BadCredentialsException.class, () -> authenticationService.signin(loginRequest));

        verify(userRepository, times(1)).findByEmail(loginRequest.getUsername());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    void testSignUpSuccess() {
        // Preparar datos
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("987654321");
        registerRequest.setAddress("San Junipero");

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Ejecutar el metodo
        JwtAuthResponse response = authenticationService.signup(registerRequest);

        // Verificar el resultado
        assertNotNull(response);
        assertNull(response.getToken());
        assertFalse(response.isEmailVerified());
        verify(userRepository, times(1)).save(any(Usuario.class));
        verify(accountTokenRepository, times(1)).save(any(AccountToken.class));
        verify(eventPublisher, times(1)).publishEvent(any()); // Verifica que se publicaron eventos
    }

    @Test
    void testSignUpUserAlreadyExists() {
        // Preparar datos
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existinguser@example.com");
        registerRequest.setPassword("password123");

        Usuario existingUser = new Usuario();
        existingUser.setEmail("existinguser@example.com");

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Verificar que se lanza la excepción de UserAlreadyExistException
        assertThrows(UserAlreadyExistException.class, () -> authenticationService.signup(registerRequest));

        verify(userRepository, never()).save(any(Usuario.class));
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }
}
