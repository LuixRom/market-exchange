package com.dbp.proyectobackendmarketexchange.usuario.domain;

import com.dbp.proyectobackendmarketexchange.auth.dto.JwtAuthResponse;
import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.event.usuario.UsuarioCreadoEvent;
import com.dbp.proyectobackendmarketexchange.exception.InvalidUserFieldException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioRequestDto;
import com.dbp.proyectobackendmarketexchange.usuario.dto.UsuarioResponseDto;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthorizationUtils authorizationUtils;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ModelMapper modelMapper, ApplicationEventPublisher eventPublisher, AuthorizationUtils authorizationUtils, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
        this.authorizationUtils = authorizationUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDto registrarUsuario(UsuarioRequestDto requestDTO) {
        // Validar nombre
        if (requestDTO.getFirstname() == null || requestDTO.getFirstname().isBlank()) {
            throw new InvalidUserFieldException("El nombre no puede estar vacío");
        }
        if (requestDTO.getFirstname().length() > 50) {
            throw new InvalidUserFieldException("El nombre no puede tener más de 50 caracteres");
        }

        // Validar apellido
        if (requestDTO.getLastname() == null || requestDTO.getLastname().isBlank()) {
            throw new InvalidUserFieldException("El apellido no puede estar vacío");
        }
        if (requestDTO.getLastname().length() > 50) {
            throw new InvalidUserFieldException("El apellido no puede tener más de 50 caracteres");
        }

        // Validar correo
        if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
            throw new InvalidUserFieldException("El correo no puede estar vacío");
        }
        if (!requestDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidUserFieldException("El correo no es válido");
        }

        // Validar teléfono
        if (requestDTO.getPhone() == null || requestDTO.getPhone().length() < 7 || requestDTO.getPhone().length() > 15) {
            throw new InvalidUserFieldException("El teléfono debe tener entre 7 y 15 dígitos");
        }

        // Validar contraseña
        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new InvalidUserFieldException("La contraseña no puede estar vacía");
        }
        if (requestDTO.getPassword().length() < 8) {
            throw new InvalidUserFieldException("La contraseña debe tener al menos 8 caracteres");
        }

        // Validar dirección
        if (requestDTO.getAddress() == null || requestDTO.getAddress().isBlank()) {
            throw new InvalidUserFieldException("La dirección no puede estar vacía");
        }
        if (requestDTO.getAddress().length() > 100) {
            throw new InvalidUserFieldException("La dirección no puede tener más de 100 caracteres");
        }

        // Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuario.setFirstname(requestDTO.getFirstname());
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setLastname(requestDTO.getLastname());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setAddress(requestDTO.getAddress());
        usuario.setPhone(requestDTO.getPhone());
        usuario = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioCreadoEvent(this, usuario));

        return modelMapper.map(usuario, UsuarioResponseDto.class);
    }

    public UsuarioResponseDto buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        return modelMapper.map(usuario, UsuarioResponseDto.class);
    }

    public List<UsuarioResponseDto> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioResponseDto.class))
                .collect(Collectors.toList());
    }

    public UsuarioResponseDto actualizarUsuario(Long id, UsuarioRequestDto requestDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        if (!authorizationUtils.isAdminOrResourceOwner(usuarioExistente.getId())) {
            throw new ForbiddenOperationException("You do not have permission to update this user.");
        }

        modelMapper.map(requestDTO, usuarioExistente);
        usuarioExistente.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuarioRepository.save(usuarioExistente);
        // Generar la respuesta con el token JWT


        return modelMapper.map(usuarioExistente, UsuarioResponseDto.class);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        if (!authorizationUtils.isAdminOrResourceOwner(usuario.getId())) {
            throw new ForbiddenOperationException("You do not have permission to delete this user.");

        }


        usuarioRepository.delete(usuario);
    }



    public UserDetailsService userDetailsService() {
        return username -> {
            Usuario user = usuarioRepository
                    .findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return (UserDetails) user;
        };
    }

    public UsuarioResponseDto getUsuarioOwnInfo() {
        // Obtener el principal del contexto de seguridad (usuario autenticado)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();  // Obtener el email del principal
        } else {
            throw new InvalidUserFieldException("No autorizado para esta operación");
        }

        // Buscar al usuario en la base de datos utilizando el email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Devolver la información del usuario mapeada al DTO de respuesta
        return modelMapper.map(usuario, UsuarioResponseDto.class);
    }





}

