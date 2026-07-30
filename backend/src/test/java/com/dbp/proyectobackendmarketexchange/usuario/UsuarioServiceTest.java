package com.dbp.proyectobackendmarketexchange.usuario;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.event.usuario.UsuarioCreadoEvent;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.domain.UsuarioService;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioRequestDto;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioResponseDto;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegistrarUsuarioExitosamente() {
        // Preparar datos
        UsuarioRequestDto requestDto = new UsuarioRequestDto();
        requestDto.setFirstname("Carlos");
        requestDto.setLastname("Lopez");
        requestDto.setEmail("carlos@example.com");
        requestDto.setPhone("987654321");
        requestDto.setAddress("Nueva direccion");
        requestDto.setPassword("Password123");
        requestDto.setLastname("Villegas");
        requestDto.setEmail("carlos@example.com");
        requestDto.setPhone("123456789");
        requestDto.setPassword("password123");
        requestDto.setAddress("Calle Falsa 123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        UsuarioResponseDto responseDto = new UsuarioResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del modelMapper y repository
        when(modelMapper.map(requestDto, Usuario.class)).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioResponseDto.class)).thenReturn(responseDto);

        // Ejecutar el metodo del servicio
        UsuarioResponseDto result = usuarioService.registrarUsuario(requestDto);

        // Verificar que se llama al repositorio y al modelMapper
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioCreadoEvent.class));
    }

    @Test
    public void testBuscarUsuarioPorIdUsuarioExistente() {
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        UsuarioResponseDto responseDto = new UsuarioResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del repositorio y modelMapper
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(modelMapper.map(usuario, UsuarioResponseDto.class)).thenReturn(responseDto);

        // Ejecutar el metodo del servicio
        UsuarioResponseDto result = usuarioService.buscarUsuarioPorId(1L);

        // Verificar el resultado
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testBuscarUsuarioPorIdNoExistente() {
        // Simular comportamiento del repositorio
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción esperada
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.buscarUsuarioPorId(1L));
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testListarUsuarios() {
        // Preparar datos
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);

        UsuarioResponseDto responseDto1 = new UsuarioResponseDto();
        responseDto1.setId(1L);
        UsuarioResponseDto responseDto2 = new UsuarioResponseDto();
        responseDto2.setId(2L);

        // Simular comportamiento del repositorio y modelMapper
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario1, usuario2));
        when(modelMapper.map(usuario1, UsuarioResponseDto.class)).thenReturn(responseDto1);
        when(modelMapper.map(usuario2, UsuarioResponseDto.class)).thenReturn(responseDto2);

        // Ejecutar el metodo del servicio
        List<UsuarioResponseDto> result = usuarioService.listarUsuarios();

        // Verificar el resultado
        assertEquals(2, result.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void testActualizarUsuarioExitosamente() {
        // Preparar datos
        UsuarioRequestDto requestDto = new UsuarioRequestDto();
        requestDto.setFirstname("Carlos");
        requestDto.setLastname("Lopez");
        requestDto.setEmail("carlos.update@example.com");
        requestDto.setPhone("987654321");
        requestDto.setAddress("Nueva direccion");
        requestDto.setPassword("Password123");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);

        UsuarioResponseDto responseDto = new UsuarioResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del repositorio, authorizationUtils y modelMapper
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        doAnswer(invocation -> {
            // Realizar el mapeo "manual" simulado de los campos
            Usuario existing = invocation.getArgument(1); // Obtiene usuarioExistente
            UsuarioRequestDto request = invocation.getArgument(0); // Obtiene requestDto

            existing.setFirstname(request.getFirstname());
            existing.setLastname(request.getLastname());
            existing.setEmail(request.getEmail());
            existing.setPhone(request.getPhone());
            existing.setAddress(request.getAddress());
            existing.setPassword(request.getPassword());

            return null;
        }).when(modelMapper).map(eq(requestDto), any(Usuario.class));
        when(usuarioRepository.save(usuarioExistente)).thenReturn(usuarioExistente);
        when(modelMapper.map(usuarioExistente, UsuarioResponseDto.class)).thenReturn(responseDto);

        // Ejecutar el metodo del servicio
        UsuarioResponseDto result = usuarioService.actualizarUsuario(1L, requestDto);

        // Verificar el resultado
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    public void testActualizarUsuarioSinAutorizacion() {
        // Preparar datos
        UsuarioRequestDto requestDto = new UsuarioRequestDto();
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);

        // Simular comportamiento del repositorio y authorizationUtils
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(false);

        // Verificar que se lanza la excepción esperada
        assertThrows(ForbiddenOperationException.class, () -> usuarioService.actualizarUsuario(1L, requestDto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void testEliminarUsuarioExitosamente() {
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        // Simular comportamiento del repositorio y authorizationUtils
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        // Ejecutar el metodo del servicio
        usuarioService.eliminarUsuario(1L);

        // Verificar que el metodo del repositorio es llamado
        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    public void testEliminarUsuarioSinAutorizacion() {
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        // Simular comportamiento del repositorio y authorizationUtils
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(false);

        // Verificar que se lanza la excepción esperada
        assertThrows(ForbiddenOperationException.class, () -> usuarioService.eliminarUsuario(1L));
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    public void testUserDetailsServiceUsuarioExistente() {
        // Preparar datos
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("carlos@example.com");

        // Simular comportamiento del repositorio
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.of(usuario));

        // Ejecutar el metodo del servicio
        UserDetails result = usuarioService.userDetailsService().loadUserByUsername("carlos@example.com");

        // Verificar el resultado
        assertNotNull(result);
        assertEquals("carlos@example.com", result.getUsername());
    }

    @Test
    public void testUserDetailsServiceUsuarioNoExistente() {
        // Simular comportamiento del repositorio
        when(usuarioRepository.findByEmail("carlos@example.com")).thenReturn(Optional.empty());

        // Verificar que se lanza la excepción esperada
        assertThrows(UsernameNotFoundException.class, () -> usuarioService.userDetailsService().loadUserByUsername("carlos@example.com"));
    }
}

