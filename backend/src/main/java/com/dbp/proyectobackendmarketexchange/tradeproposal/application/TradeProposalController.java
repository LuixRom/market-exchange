package com.dbp.proyectobackendmarketexchange.tradeproposal.application;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposalService;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalRequestDto;
import com.dbp.proyectobackendmarketexchange.tradeproposal.dto.TradeProposalResponseDto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agreements")
public class TradeProposalController {

    private final TradeProposalService tradeProposalService;

    public TradeProposalController(TradeProposalService tradeProposalService) {
        this.tradeProposalService = tradeProposalService;
    }

    @GetMapping
    public ResponseEntity<List<TradeProposalResponseDto>> getAllTradeProposals() {
        return ResponseEntity.ok(tradeProposalService.getAllTradeProposals());
    }

    @PostMapping
    public ResponseEntity<TradeProposalResponseDto> createTradeProposal(@Valid @RequestBody TradeProposalRequestDto requestDto) {
        TradeProposalResponseDto responseDto = tradeProposalService.createTradeProposal(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeProposalResponseDto> getTradeProposalById(@PathVariable Long id) {
        return ResponseEntity.ok(tradeProposalService.getTradeProposalById(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<TradeProposalResponseDto> acceptTradeProposal(@PathVariable Long id) {
        return ResponseEntity.ok(tradeProposalService.acceptTradeProposal(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TradeProposalResponseDto> rejectTradeProposal(@PathVariable Long id) {
        return ResponseEntity.ok(tradeProposalService.rejectTradeProposal(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTradeProposal(@PathVariable Long id) {
        tradeProposalService.deleteTradeProposal(id);
        return ResponseEntity.noContent().build();
    }
}
