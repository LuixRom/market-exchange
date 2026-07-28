package com.dbp.proyectobackendmarketexchange.tradeproposal;

import com.dbp.proyectobackendmarketexchange.tradeproposal.application.TradeProposalController;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposalService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeStatus;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalRequestDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TradeProposalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeProposalService tradeProposalService;

    @InjectMocks
    private TradeProposalController tradeProposalController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tradeProposalController).build();
    }

    @Test
    public void testGetAllTradeProposals() throws Exception {
        TradeProposalResponseDto dto1 = new TradeProposalResponseDto();
        dto1.setId(1L);
        dto1.setStatus(TradeStatus.PENDING);

        TradeProposalResponseDto dto2 = new TradeProposalResponseDto();
        dto2.setId(2L);
        dto2.setStatus(TradeStatus.ACCEPTED);

        when(tradeProposalService.getAllTradeProposals()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/agreements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("ACCEPTED"));

        verify(tradeProposalService, times(1)).getAllTradeProposals();
    }

    @Test
    public void testGetTradeProposalById() throws Exception {
        TradeProposalResponseDto dto = new TradeProposalResponseDto();
        dto.setId(1L);
        dto.setStatus(TradeStatus.PENDING);

        when(tradeProposalService.getTradeProposalById(1L)).thenReturn(dto);

        mockMvc.perform(get("/agreements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(tradeProposalService, times(1)).getTradeProposalById(1L);
    }

    @Test
    public void testCreateTradeProposal() throws Exception {
        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();
        requestDto.setOfferedItemId(10L);
        requestDto.setRequestedItemId(20L);

        TradeProposalResponseDto responseDto = new TradeProposalResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(TradeStatus.PENDING);

        when(tradeProposalService.createTradeProposal(any(TradeProposalRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(tradeProposalService, times(1)).createTradeProposal(any(TradeProposalRequestDto.class));
    }

    @Test
    public void testCreateTradeProposal_MissingFields_BadRequest() throws Exception {
        TradeProposalRequestDto requestDto = new TradeProposalRequestDto();

        mockMvc.perform(post("/agreements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(tradeProposalService, never()).createTradeProposal(any());
    }

    @Test
    public void testAcceptTradeProposal() throws Exception {
        TradeProposalResponseDto responseDto = new TradeProposalResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(TradeStatus.ACCEPTED);

        when(tradeProposalService.acceptTradeProposal(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/agreements/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(tradeProposalService, times(1)).acceptTradeProposal(1L);
    }

    @Test
    public void testRejectTradeProposal() throws Exception {
        TradeProposalResponseDto responseDto = new TradeProposalResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(TradeStatus.REJECTED);

        when(tradeProposalService.rejectTradeProposal(1L)).thenReturn(responseDto);

        mockMvc.perform(put("/agreements/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(tradeProposalService, times(1)).rejectTradeProposal(1L);
    }

    @Test
    public void testDeleteTradeProposal() throws Exception {
        doNothing().when(tradeProposalService).deleteTradeProposal(1L);

        mockMvc.perform(delete("/agreements/1"))
                .andExpect(status().isNoContent());

        verify(tradeProposalService, times(1)).deleteTradeProposal(1L);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
