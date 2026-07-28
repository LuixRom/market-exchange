package com.dbp.proyectobackendmarketexchange.raiting;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.rating.application.RatingController;
import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)  // Deshabilitar filtros de seguridad en pruebas
public class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser // Simula un usuario autenticado
    public void testCrearRating() throws Exception {
        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setUsuarioId(1L);
        requestDto.setRaterUsuarioId(2L);
        requestDto.setRating(4);
        requestDto.setComment("Buen servicio");

        RatingResponseDto responseDto = new RatingResponseDto();
        responseDto.setId(1L);

        when(ratingService.crearRating(any(RatingRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/ratings/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser // Simula un usuario autenticado
    public void testListarRatings() throws Exception {
        List<RatingResponseDto> responseDtos = new ArrayList<>();
        when(ratingService.listarRatings()).thenReturn(responseDtos);

        mockMvc.perform(get("/ratings/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser // Simula un usuario autenticado
    public void testObtenerRatingsPorUsuario() throws Exception {
        List<RatingResponseDto> responseDtos = new ArrayList<>();
        when(ratingService.obtenerRatingsPorUsuario(1L)).thenReturn(responseDtos);

        mockMvc.perform(get("/ratings/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testDeleteRating() throws Exception {
        mockMvc.perform(delete("/ratings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
