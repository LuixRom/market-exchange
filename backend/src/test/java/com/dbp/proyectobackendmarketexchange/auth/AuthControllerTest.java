package com.dbp.proyectobackendmarketexchange.auth;

import com.dbp.proyectobackendmarketexchange.auth.application.AuthController;
import com.dbp.proyectobackendmarketexchange.auth.domain.AuthenticationService;
import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.dto.LoginRequest;
import com.dbp.proyectobackendmarketexchange.auth.dto.RegisterRequest;
import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // Deshabilitar filtros de seguridad en pruebas
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtService jwtService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // Preparar datos
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("user@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("987654321");
        registerRequest.setAddress("Test Address");

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken("fake-jwt-token");

        // Simular comportamiento del servicio de autenticación
        when(authenticationService.signup(any(RegisterRequest.class))).thenReturn(jwtAuthResponse);

        // Realizar la solicitud POST y verificar la respuesta
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));

        // Verificar que el servicio fue llamado
        verify(authenticationService, times(1)).signup(any(RegisterRequest.class));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // Preparar datos
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user@example.com");
        loginRequest.setPassword("password123");

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken("fake-jwt-token");

        // Simular comportamiento del servicio de autenticación
        when(authenticationService.signin(any(LoginRequest.class))).thenReturn(jwtAuthResponse);

        // Realizar la solicitud POST y verificar la respuesta
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));

        // Verificar que el servicio fue llamado
        verify(authenticationService, times(1)).signin(any(LoginRequest.class));
    }
}
