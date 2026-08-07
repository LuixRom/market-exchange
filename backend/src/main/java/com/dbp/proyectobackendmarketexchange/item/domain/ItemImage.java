package com.dbp.proyectobackendmarketexchange.item.domain;

import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "item_image", indexes = {
        @Index(name = "idx_item_image_item", columnList = "item_id"),
        @Index(name = "idx_item_image_storage", columnList = "storage_key")
})
public class ItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 20)
    private StorageProvider storageProvider;

    @Column(nullable = false)
    private boolean primaryImage;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(Clock.systemDefaultZone());
    }
}
