package com.dbp.proyectobackendmarketexchange.tradeproposal.domain;

import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "trade_proposal")
public class TradeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    @NotNull(message = "El estado no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private TradeStatus status = TradeStatus.PENDING;

    @OneToOne(mappedBy = "tradeProposal", cascade = CascadeType.ALL)
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "offered_item_id")
    @NotNull(message = "El item ofrecido no puede ser nulo")
    private Item offeredItem;

    @ManyToOne
    @JoinColumn(name = "requested_item_id")
    @NotNull(message = "El item solicitado no puede ser nulo")
    private Item requestedItem;

    @ManyToOne
    @JoinColumn(name = "proposer_id")
    @NotNull(message = "El proponente no puede ser nulo")
    private Usuario proposer;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @NotNull(message = "El receptor no puede ser nulo")
    private Usuario receiver;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
