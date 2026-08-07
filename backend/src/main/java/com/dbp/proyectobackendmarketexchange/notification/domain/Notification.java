package com.dbp.proyectobackendmarketexchange.notification.domain;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Usuario recipient;

    @Column(nullable = false, length = 80)
    private String type;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    private Long tradeProposalId;
    private Long itemId;

    @Column(nullable = false)
    private boolean readFlag = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(Clock.systemDefaultZone());
    }
}
