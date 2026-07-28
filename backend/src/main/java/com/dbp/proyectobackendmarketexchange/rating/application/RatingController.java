package com.dbp.proyectobackendmarketexchange.rating.application;


import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<RatingResponseDto> deleteRating(@PathVariable("id") Long ratingId) {
        ratingService.deleteItem(ratingId);
        return ResponseEntity.noContent().build();
    }
}