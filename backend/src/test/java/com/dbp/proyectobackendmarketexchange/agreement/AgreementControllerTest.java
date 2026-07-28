package com.dbp.proyectobackendmarketexchange.agreement;

import com.dbp.proyectobackendmarketexchange.agreement.application.AgreementController;
import com.dbp.proyectobackendmarketexchange.agreement.domain.AgreementService;
import com.dbp.proyectobackendmarketexchange.agreement.domain.State;
import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementRequestDto;
import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class AgreementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AgreementService agreementService;

    @InjectMocks
    private AgreementController agreementController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(agreementController).build();
    }

    @Test
    public void testGetAllAgreements() throws Exception {
        AgreementResponseDto agreement1 = new AgreementResponseDto();
        agreement1.setId(1L);
        agreement1.setState(State.PENDING);  // Cambia "PENDING" por State.PENDING

        AgreementResponseDto agreement2 = new AgreementResponseDto();
        agreement2.setId(2L);
        agreement2.setState(State.ACCEPTED);  // Cambia "ACCEPTED" por State.ACCEPTED

        when(agreementService.getAllAgreements()).thenReturn(List.of(agreement1, agreement2));

        mockMvc.perform(get("/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].state").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].state").value("ACCEPTED"))
                .andDo(print());

        verify(agreementService, times(1)).getAllAgreements();
    }

    @Test
    public void testGetAgreementById() throws Exception {
        AgreementResponseDto agreement = new AgreementResponseDto();
        agreement.setId(1L);
        agreement.setState(State.PENDING);  // Cambia "PENDING" por State.PENDING

        when(agreementService.getAgreementById(1L)).thenReturn(agreement);

        mockMvc.perform(get("/agreements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andDo(print());

        verify(agreementService, times(1)).getAgreementById(1L);
    }

    @Test
    public void testCreateAgreement() throws Exception {
        AgreementRequestDto requestDto = new AgreementRequestDto();
        requestDto.setState(State.PENDING);  // Cambia "PENDING" por State.PENDING
        requestDto.setItemIniId(1L);
        requestDto.setItemFinId(2L);
        requestDto.setUsuarioIniId(1L);
        requestDto.setUsuarioFinId(2L);

        AgreementResponseDto responseDto = new AgreementResponseDto();
        responseDto.setId(1L);
        responseDto.setState(State.PENDING);  // Cambia "PENDING" por State.PENDING

        when(agreementService.createAgreement(any(AgreementRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andDo(print());

        verify(agreementService, times(1)).createAgreement(any(AgreementRequestDto.class));
    }

    @Test
    public void testUpdateAgreement() throws Exception {
        AgreementRequestDto requestDto = new AgreementRequestDto();
        requestDto.setState(State.ACCEPTED);  // Cambia "ACCEPTED" por State.ACCEPTED
        requestDto.setItemIniId(1L);
        requestDto.setItemFinId(2L);
        requestDto.setUsuarioIniId(1L);
        requestDto.setUsuarioFinId(2L);

        AgreementResponseDto responseDto = new AgreementResponseDto();
        responseDto.setId(1L);
        responseDto.setState(State.ACCEPTED);  // Cambia "ACCEPTED" por State.ACCEPTED

        when(agreementService.updateAgreement(eq(1L), any(AgreementRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/agreements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("ACCEPTED"))
                .andDo(print());

        verify(agreementService, times(1)).updateAgreement(eq(1L), any(AgreementRequestDto.class));
    }

    @Test
    public void testAcceptAgreement() throws Exception {
        AgreementResponseDto responseDto = new AgreementResponseDto();
        responseDto.setId(1L);
        responseDto.setState(State.ACCEPTED);  // Cambia "ACCEPTED" por State.ACCEPTED

        when(agreementService.acceptAgreement(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/agreements/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("ACCEPTED"))  // Cambia "status" por "state"
                .andDo(print());

        verify(agreementService, times(1)).acceptAgreement(1L);
    }

    @Test
    public void testRejectAgreement() throws Exception {
        AgreementResponseDto responseDto = new AgreementResponseDto();
        responseDto.setId(1L);
        responseDto.setState(State.REJECTED);  // Cambia "REJECTED" por State.REJECTED

        when(agreementService.rejectAgreement(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/agreements/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("REJECTED"))  // Cambia "status" por "state"
                .andDo(print());

        verify(agreementService, times(1)).rejectAgreement(1L);
    }

    @Test
    public void testDeleteAgreement() throws Exception {
        doNothing().when(agreementService).deleteAgreement(1L);

        mockMvc.perform(delete("/agreements/1"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(agreementService, times(1)).deleteAgreement(1L);
    }

    // Método auxiliar para convertir objetos a JSON
    public static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
