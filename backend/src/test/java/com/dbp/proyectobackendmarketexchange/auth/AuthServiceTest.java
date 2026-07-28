package com.dbp.proyectobackendmarketexchange.auth;

import com.dbp.proyectobackendmarketexchange.auth.domain.AuthenticationService;
import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;
import com.dbp.proyectobackendmarketexchange.auth.exception.UserAlreadyExistException;
import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignInSuccess() {
        // Preparar datos
        Usuario user = new Usuario();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

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

        // Verificar que se lanza la excepción de UsernameNotFoundException
        assertThrows(UsernameNotFoundException.class, () -> authenticationService.signin(loginRequest));

        verify(userRepository, times(1)).findByEmail(loginRequest.getUsername());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    void testSignInInvalidPassword() {
        // Preparar datos
        Usuario user = new Usuario();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Verificar que se lanza la excepción de IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> authenticationService.signin(loginRequest));

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

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake-jwt-token");

        // Ejecutar el metodo
        JwtAuthResponse response = authenticationService.signup(registerRequest);

        // Verificar el resultado
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        verify(userRepository, times(1)).save(any(Usuario.class));
        verify(jwtService, times(1)).generateToken(any(Usuario.class));
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

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));

        // Verificar que se lanza la excepción de UserAlreadyExistException
        assertThrows(UserAlreadyExistException.class, () -> authenticationService.signup(registerRequest));

        verify(userRepository, never()).save(any(Usuario.class));
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }
}
