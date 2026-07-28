package com.dbp.proyectobackendmarketexchange.rating.infrastructure;

import com.dbp.proyectobackendmarketexchange.rating.domain.Rating;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByUsuario(Usuario usuario);
}
