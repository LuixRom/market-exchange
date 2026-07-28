package com.dbp.proyectobackendmarketexchange.rating.domain;

import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;



@Getter
@Setter
@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación no puede ser mayor que 5")
    private int rating;

    @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres")
    private String comment;

    private LocalDateTime createdAt;


    @ManyToOne
    @JoinColumn(name = "rater_usuario_id")
    private Usuario raterUsuario;


    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
