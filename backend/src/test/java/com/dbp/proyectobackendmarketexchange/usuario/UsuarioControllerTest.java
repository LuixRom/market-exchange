package com.dbp.proyectobackendmarketexchange.usuario;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.usuario.application.UsuarioController;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioRequestDto;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)  // Deshabilitar filtros de seguridad en pruebas
@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtService jwtService;  // Si tu aplicación utiliza JWT para autenticación, asegúrate de mockearlo

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void testObtenerUsuarioPorId() throws Exception {
        UsuarioResponseDto usuarioResponseDto = new UsuarioResponseDto();
        usuarioResponseDto.setId(1L);
        usuarioResponseDto.setFirstname("Carlos");
        usuarioResponseDto.setLastname("Villegas");

        when(usuarioService.buscarUsuarioPorId(anyLong())).thenReturn(usuarioResponseDto);

        mockMvc.perform(get("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Carlos"))
                .andExpect(jsonPath("$.lastname").value("Villegas"));

        verify(usuarioService, times(1)).buscarUsuarioPorId(1L);
    }

    @Test
    public void testListarUsuarios() throws Exception {
        UsuarioResponseDto usuario1 = new UsuarioResponseDto();
        usuario1.setId(1L);
        usuario1.setFirstname("Carlos");
        usuario1.setLastname("Villegas");

        UsuarioResponseDto usuario2 = new UsuarioResponseDto();
        usuario2.setId(2L);
        usuario2.setFirstname("Maria");
        usuario2.setLastname("Perez");

        List<UsuarioResponseDto> usuarios = Arrays.asList(usuario1, usuario2);

        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/usuarios/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstname").value("Carlos"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstname").value("Maria"));

        verify(usuarioService, times(1)).listarUsuarios();
    }

    @Test
    public void testActualizarUsuario() throws Exception {
        UsuarioRequestDto requestDto = new UsuarioRequestDto();
        requestDto.setFirstname("Carlos");
        requestDto.setLastname("Villegas");

        UsuarioResponseDto responseDto = new UsuarioResponseDto();
        responseDto.setId(1L);
        responseDto.setFirstname("Carlos");
        responseDto.setLastname("Villegas");

        when(usuarioService.actualizarUsuario(anyLong(), any(UsuarioRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Carlos"))
                .andExpect(jsonPath("$.lastname").value("Villegas"));

        verify(usuarioService, times(1)).actualizarUsuario(anyLong(), any(UsuarioRequestDto.class));
    }

    @Test
    public void testEliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(anyLong());

        mockMvc.perform(delete("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario(1L);
    }

    @Test
    public void testGetMyInfo() throws Exception {
        UsuarioResponseDto usuarioResponseDto = new UsuarioResponseDto();
        usuarioResponseDto.setId(1L);
        usuarioResponseDto.setFirstname("Carlos");
        usuarioResponseDto.setLastname("Villegas");

        when(usuarioService.getUsuarioOwnInfo()).thenReturn(usuarioResponseDto);

        mockMvc.perform(get("/usuarios/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Carlos"))
                .andExpect(jsonPath("$.lastname").value("Villegas"));

        verify(usuarioService, times(1)).getUsuarioOwnInfo();
    }
}
