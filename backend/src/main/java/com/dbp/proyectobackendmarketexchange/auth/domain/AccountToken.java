package com.dbp.proyectobackendmarketexchange.auth.domain;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "account_token", indexes = {
        @Index(name = "idx_account_token_token", columnList = "token", unique = true),
        @Index(name = "idx_account_token_user_type", columnList = "usuario_id,type")
})
public class AccountToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccountTokenType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isActive(LocalDateTime now) {
        return revokedAt == null && usedAt == null && expiresAt.isAfter(now);
    }
}
