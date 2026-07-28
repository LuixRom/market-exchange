package com.dbp.proyectobackendmarketexchange.item.domain;

import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String name;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String description;

    @NotNull(message = "La categoría no puede ser nula")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;


    @Enumerated(EnumType.STRING)
    @NotNull(message = "La condición del ítem no puede ser nula")
    private Condition condition;



    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // estado de item (Pendiente, aprobado, denegado

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @NotNull(message = "El usuario no puede ser nulo")
    private Usuario usuario;

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

    private String imagePath;


}