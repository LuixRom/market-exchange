package com.dbp.proyectobackendmarketexchange.item.infrastructure;

import com.dbp.proyectobackendmarketexchange.item.domain.FavoriteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
    boolean existsByUsuarioIdAndItemId(Long usuarioId, Long itemId);
    Optional<FavoriteItem> findByUsuarioIdAndItemId(Long usuarioId, Long itemId);
    List<FavoriteItem> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
