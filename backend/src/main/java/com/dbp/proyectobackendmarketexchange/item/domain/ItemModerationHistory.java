package com.dbp.proyectobackendmarketexchange.item.domain;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "item_moderation_history")
public class ItemModerationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    @NotNull
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "moderator_id")
    @NotNull
    private Usuario moderator;

    @Enumerated(EnumType.STRING)
    private ItemStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ItemStatus newStatus;

    @Size(max = 500)
    private String reason;

    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
