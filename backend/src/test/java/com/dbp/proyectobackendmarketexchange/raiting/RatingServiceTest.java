package com.dbp.proyectobackendmarketexchange.raiting;


import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.rating.domain.Rating;
import com.dbp.proyectobackendmarketexchange.rating.domain.RatingService;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingRequestDto;
import com.dbp.proyectobackendmarketexchange.rating.dto.RatingResponseDto;
import com.dbp.proyectobackendmarketexchange.rating.infrastructure.RatingRepository;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @InjectMocks
    private RatingService ratingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearRatingExitosamente() {
        RatingRequestDto requestDto = new RatingRequestDto();
        requestDto.setUsuarioId(1L);
        requestDto.setRaterUsuarioId(2L);
        requestDto.setRating(4);
        requestDto.setComment("Buen servicio");

        Usuario usuarioCalificado = new Usuario();
        usuarioCalificado.setId(1L);
        usuarioCalificado.setFirstname("Luis");
        usuarioCalificado.setLastname("Perez");

        Usuario raterUsuario = new Usuario();
        raterUsuario.setId(2L);
        raterUsuario.setFirstname("Juan");
        raterUsuario.setLastname("Lopez");

        Rating rating = new Rating();
        rating.setId(1L);
        rating.setUsuario(usuarioCalificado);
        rating.setRaterUsuario(raterUsuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioCalificado));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(raterUsuario));
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);

        RatingResponseDto responseDto = new RatingResponseDto();
        responseDto.setId(1L);

        when(modelMapper.map(any(Rating.class), eq(RatingResponseDto.class))).thenReturn(responseDto);

        RatingResponseDto result = ratingService.crearRating(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ratingRepository, times(1)).save(any(Rating.class));
    }

    @Test
    public void testListarRatings() {
        // Crear objetos Rating simulados
        Rating rating1 = new Rating();
        rating1.setId(1L);
        rating1.setRating(5);
        Usuario usuario1 = new Usuario();
        usuario1.setFirstname("Luis");
        usuario1.setLastname("Perez");
        rating1.setUsuario(usuario1);  // Asociar el usuario calificado al rating
        rating1.setRaterUsuario(usuario1);  // Asegurar que el RaterUsuario también está asignado

        Rating rating2 = new Rating();
        rating2.setId(2L);
        rating2.setRating(4);
        Usuario usuario2 = new Usuario();
        usuario2.setFirstname("Juan");
        usuario2.setLastname("Lopez");
        rating2.setUsuario(usuario2);  // Asociar el usuario calificado al rating
        rating2.setRaterUsuario(usuario2);  // Asegurar que el RaterUsuario también está asignado

        // Simular la respuesta del repositorio
        List<Rating> ratings = List.of(rating1, rating2);
        when(ratingRepository.findAll()).thenReturn(ratings);

        // Configurar los mapeos de Rating a RatingResponseDto
        RatingResponseDto responseDto1 = new RatingResponseDto();
        responseDto1.setId(1L);
        responseDto1.setRating(5);
        responseDto1.setUsuarioNombre("Luis Perez");

        RatingResponseDto responseDto2 = new RatingResponseDto();
        responseDto2.setId(2L);
        responseDto2.setRating(4);
        responseDto2.setUsuarioNombre("Juan Lopez");

        // Simular los mapeos del modelMapper
        when(modelMapper.map(rating1, RatingResponseDto.class)).thenReturn(responseDto1);
        when(modelMapper.map(rating2, RatingResponseDto.class)).thenReturn(responseDto2);

        // Ejecutar el método del servicio
        List<RatingResponseDto> result = ratingService.listarRatings();

        // Verificar los resultados
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(5, result.get(0).getRating());
        assertEquals("Luis Perez", result.get(0).getUsuarioNombre());

        assertEquals(2L, result.get(1).getId());
        assertEquals(4, result.get(1).getRating());
        assertEquals("Juan Lopez", result.get(1).getUsuarioNombre());

        // Verificar que el repositorio se llamó una vez
        verify(ratingRepository, times(1)).findAll();
    }



    @Test
    public void testObtenerRatingsPorUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        ratingService.obtenerRatingsPorUsuario(1L);

        verify(ratingRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    public void testDeleteRatingUnauthorized() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Rating rating = new Rating();
        rating.setId(1L);
        rating.setUsuario(usuario);  // Evitamos NullPointerException

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));
        when(authorizationUtils.isAdminOrResourceOwner(usuario.getId())).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> {
            ratingService.deleteItem(1L);
        });
    }

}
