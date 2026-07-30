package com.dbp.proyectobackendmarketexchange.item.infrastructure;

import com.dbp.proyectobackendmarketexchange.item.domain.ItemModerationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemModerationHistoryRepository extends JpaRepository<ItemModerationHistory, Long> {
    List<ItemModerationHistory> findByItemIdOrderByCreatedAtDesc(Long itemId);
}
