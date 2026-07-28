package com.dbp.proyectobackendmarketexchange.raiting;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.rating.application.RatingController;
import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingReputationDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

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
    @WithMockUser
    public void testCrearRating() throws Exception {
        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setTradeProposalId(1L);
        requestDto.setScore(4);
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
    @WithMockUser
    public void testListarRatings() throws Exception {
        List<RatingResponseDto> responseDtos = new ArrayList<>();
        when(ratingService.listarRatings()).thenReturn(responseDtos);

        mockMvc.perform(get("/ratings/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testObtenerRatingsPorUsuario() throws Exception {
        List<RatingResponseDto> responseDtos = new ArrayList<>();
        when(ratingService.obtenerRatingsPorUsuario(1L)).thenReturn(responseDtos);

        mockMvc.perform(get("/ratings/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetReputation() throws Exception {
        RatingReputationDto reputation = new RatingReputationDto();
        reputation.setUserId(1L);
        reputation.setAverageScore(4.5);
        reputation.setRatingCount(2L);

        when(ratingService.getReputation(1L)).thenReturn(reputation);

        mockMvc.perform(get("/ratings/usuario/1/reputation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(4.5))
                .andExpect(jsonPath("$.ratingCount").value(2));
    }

    @Test
    @WithMockUser
    public void testDeleteRating() throws Exception {
        doNothing().when(ratingService).deleteRating(1L);

        mockMvc.perform(delete("/ratings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
