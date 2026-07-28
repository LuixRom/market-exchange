package com.dbp.proyectobackendmarketexchange.agreement.application;

import com.dbp.proyectobackendmarketexchange.agreement.domain.AgreementService;

import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementRequestDto;
import com.dbp.proyectobackendmarketexchange.agreement.dto.AgreementResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/agreements")
public class AgreementController {

    private final AgreementService agreementService;

    public AgreementController(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    @GetMapping
    public ResponseEntity<List<AgreementResponseDto>>  getAllAgreements() {

        return ResponseEntity.ok(agreementService.getAllAgreements());
    }

    @PostMapping
    public ResponseEntity<AgreementResponseDto> createAgreement(@Valid @RequestBody AgreementRequestDto agreementRequestDto) {

        AgreementResponseDto responseDto = agreementService.createAgreement(agreementRequestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgreementResponseDto> getAgreementById(@PathVariable Long id) {
        AgreementResponseDto responseDto = agreementService.getAgreementById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgreementResponseDto> updateAgreement(@PathVariable Long id, @Valid @RequestBody AgreementRequestDto agreementRequestDto) {
        AgreementResponseDto responseDto = agreementService.updateAgreement(id, agreementRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<AgreementResponseDto> acceptAgreement(@PathVariable Long id) {
        AgreementResponseDto responseDto = agreementService.acceptAgreement(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<AgreementResponseDto> rejectAgreement(@PathVariable Long id) {
        AgreementResponseDto responseDto = agreementService.rejectAgreement(id);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgreement(@PathVariable Long id) {
        agreementService.deleteAgreement(id);
        return ResponseEntity.noContent().build();
    }
}