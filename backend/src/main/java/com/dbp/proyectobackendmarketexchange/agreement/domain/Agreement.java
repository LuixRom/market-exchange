package com.dbp.proyectobackendmarketexchange.agreement.domain;

import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.shipment.domain.Shipment;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    

    import jakarta.validation.constraints.NotNull;
    import java.time.LocalDateTime;
    
    @Getter
    @Setter
    @Entity
    public class Agreement {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Version
        private Integer version;


        @NotNull(message = "El estado no puede ser nulo")
        @Enumerated(EnumType.STRING)
        private State state = State.PENDING;
    


        @OneToOne(mappedBy = "agreement", cascade = CascadeType.ALL)
        private Shipment shipment;
    
        @OneToOne
        @NotNull(message = "El objeto ofrecido por el iniciador no puede ser nulo")
        private Item item_ini;
    
        @OneToOne
        @NotNull(message = "El objeto ofrecido por el receptor no puede ser nulo")
        private Item item_fin;
    
        @ManyToOne
        @NotNull(message = "El usuario iniciador no puede ser nulo")
        private Usuario initiator;
    
        @ManyToOne
        @NotNull(message = "El usuario receptor no puede ser nulo")
        private Usuario recipient;

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