package com.dbp.proyectobackendmarketexchange.rating.domain;

import com.dbp.proyectobackendmarketexchange.tradeproposal.domain.TradeProposal;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uq_rating_trade_proposal_reviewer",
        columnNames = {"trade_proposal_id", "reviewer_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trade_proposal_id")
    @NotNull(message = "La propuesta de intercambio no puede ser nula")
    private TradeProposal tradeProposal;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    @NotNull(message = "El reviewer no puede ser nulo")
    private Usuario reviewer;

    @ManyToOne
    @JoinColumn(name = "reviewed_user_id")
    @NotNull(message = "El usuario calificado no puede ser nulo")
    private Usuario reviewedUser;

    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede ser mayor que 5")
    private int score;

    @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres")
    private String comment;

    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
