package com.dbp.proyectobackendmarketexchange.usuario.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.event.usuario.UsuarioCreadoEvent;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.InvalidUserFieldException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.usuario.dto.ProfileUpdateRequest;
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

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ModelMapper modelMapper,
                          ApplicationEventPublisher eventPublisher,
                          AuthorizationUtils authorizationUtils,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
        this.authorizationUtils = authorizationUtils;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDto registrarUsuario(UsuarioRequestDto requestDTO) {
        Usuario usuario = new Usuario();
        usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuario.setFirstname(requestDTO.getFirstname().trim());
        usuario.setLastname(requestDTO.getLastname().trim());
        usuario.setEmail(EmailNormalizer.normalize(requestDTO.getEmail()));
        usuario.setAddress(requestDTO.getAddress().trim());
        usuario.setPhone(requestDTO.getPhone().trim());
        usuario.setRole(Role.USER);
        usuario.setCreatedAt(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioCreadoEvent(this, usuario));
        return mapToResponse(usuario);
    }

    public UsuarioResponseDto buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        return mapToResponse(usuario);
    }

    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDto actualizarUsuario(Long id, UsuarioRequestDto requestDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        if (!authorizationUtils.isAdminOrResourceOwner(usuarioExistente.getId())) {
            throw new ForbiddenOperationException("No tienes permiso para actualizar este usuario.");
        }

        usuarioExistente.setFirstname(requestDTO.getFirstname().trim());
        usuarioExistente.setLastname(requestDTO.getLastname().trim());
        usuarioExistente.setEmail(EmailNormalizer.normalize(requestDTO.getEmail()));
        usuarioExistente.setPhone(requestDTO.getPhone().trim());
        usuarioExistente.setAddress(requestDTO.getAddress().trim());
        usuarioExistente.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuarioRepository.save(usuarioExistente);

        return mapToResponse(usuarioExistente);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        if (!authorizationUtils.isAdminOrResourceOwner(usuario.getId())) {
            throw new ForbiddenOperationException("No tienes permiso para eliminar este usuario.");
        }

        usuarioRepository.delete(usuario);
    }

    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository
                .findByEmail(EmailNormalizer.normalize(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UsuarioResponseDto getUsuarioOwnInfo() {
        return mapToResponse(getCurrentUsuario());
    }

    public UsuarioResponseDto updateMyProfile(ProfileUpdateRequest request) {
        Usuario usuario = getCurrentUsuario();
        usuario.setBio(request.getBio());
        usuario.setAvatarUrl(request.getAvatarUrl());
        usuario.setLocation(request.getLocation());
        usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    public UsuarioResponseDto suspendUser(Long id, String reason) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        usuario.setSuspended(true);
        usuario.setSuspendedAt(LocalDateTime.now());
        usuario.setSuspensionReason(normalizeOptional(reason));
        return mapToResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDto unsuspendUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        usuario.setSuspended(false);
        usuario.setSuspendedAt(null);
        usuario.setSuspensionReason(null);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDto blockUser(Long id, String reason) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        usuario.setBlocked(true);
        usuario.setBlockedAt(LocalDateTime.now());
        usuario.setBlockedReason(normalizeOptional(reason));
        return mapToResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDto unblockUser(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        usuario.setBlocked(false);
        usuario.setBlockedAt(null);
        usuario.setBlockedReason(null);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    private Usuario getCurrentUsuario() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new InvalidUserFieldException("No autorizado para esta operacion");
        }

        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UsuarioResponseDto mapToResponse(Usuario usuario) {
        UsuarioResponseDto dto = modelMapper.map(usuario, UsuarioResponseDto.class);
        if (usuario.getRole() != null) {
            dto.setRole(usuario.getRole().name());
        }
        return dto;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
