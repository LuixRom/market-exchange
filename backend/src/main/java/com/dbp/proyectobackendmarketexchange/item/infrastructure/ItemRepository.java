package com.dbp.proyectobackendmarketexchange.item.infrastructure;


import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategoryId(Long category_id);
    List<Item> findByUsuarioId(Long user_id);
}
