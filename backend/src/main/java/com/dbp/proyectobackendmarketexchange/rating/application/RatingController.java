package com.dbp.proyectobackendmarketexchange.rating.application;


import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingReputationDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Endpoint para crear una nueva calificación
    @PostMapping("/crear")
    public ResponseEntity<RatingResponseDto> crearRating(@Valid @RequestBody RatingRequestDto requestDTO) {
        RatingResponseDto nuevoRating = ratingService.crearRating(requestDTO);
        return new ResponseEntity<>(nuevoRating, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingResponseDto> actualizarRating(@PathVariable("id") Long ratingId,
                                                              @Valid @RequestBody RatingRequestDto requestDTO) {
        return ResponseEntity.ok(ratingService.actualizarRating(ratingId, requestDTO));
    }

    // Endpoint para listar todas las calificaciones
    @GetMapping("/listar")
    public ResponseEntity<List<RatingResponseDto>> listarRatings() {
        List<RatingResponseDto> ratings = ratingService.listarRatings();
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    // Endpoint para obtener todas las calificaciones de un usuario específico
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<RatingResponseDto>> obtenerRatingsPorUsuario(@PathVariable Long usuarioId) {
        List<RatingResponseDto> ratings = ratingService.obtenerRatingsPorUsuario(usuarioId);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}/reputation")
    public ResponseEntity<RatingReputationDto> getReputation(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ratingService.getReputation(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable("id") Long ratingId) {
        ratingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }
}
