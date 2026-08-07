package com.dbp.proyectobackendmarketexchange.notification.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.notification.dto.NotificationResponseDto;
import com.dbp.proyectobackendmarketexchange.notification.infrastructure.NotificationRepository;
import com.dbp.proyectobackendmarketexchange.realtime.RealtimeMessagingService;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UsuarioRepository usuarioRepository;
    private final RealtimeMessagingService realtimeMessagingService;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository,
                               UsuarioRepository usuarioRepository,
                               RealtimeMessagingService realtimeMessagingService,
                               Clock clock) {
        this.notificationRepository = notificationRepository;
        this.usuarioRepository = usuarioRepository;
        this.realtimeMessagingService = realtimeMessagingService;
        this.clock = clock;
    }

    public Notification create(Usuario recipient, String type, String title, String message, Long tradeProposalId, Long itemId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTradeProposalId(tradeProposalId);
        notification.setItemId(itemId);
        Notification saved = notificationRepository.save(notification);
        realtimeMessagingService.sendNotification(recipient, mapToDto(saved));
        return saved;
    }

    public List<NotificationResponseDto> listMine(boolean unreadOnly) {
        Usuario current = currentUser();
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByRecipientIdAndReadFlagFalseOrderByCreatedAtDesc(current.getId())
                : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(current.getId());
        return notifications.stream().map(this::mapToDto).toList();
    }

    public NotificationResponseDto markAsRead(Long id) {
        Usuario current = currentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada"));
        if (!notification.getRecipient().getId().equals(current.getId())) {
            throw new ForbiddenOperationException("No tienes permiso sobre esta notificacion");
        }
        notification.setReadFlag(true);
        notification.setReadAt(LocalDateTime.now(clock));
        return mapToDto(notificationRepository.save(notification));
    }

    public long countUnreadMine() {
        return notificationRepository.countByRecipientIdAndReadFlagFalse(currentUser().getId());
    }

    private Usuario currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(((UserDetails) principal).getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private NotificationResponseDto mapToDto(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setTradeProposalId(notification.getTradeProposalId());
        dto.setItemId(notification.getItemId());
        dto.setRead(notification.isReadFlag());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}
