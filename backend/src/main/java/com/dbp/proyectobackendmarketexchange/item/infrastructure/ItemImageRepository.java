package com.dbp.proyectobackendmarketexchange.item.infrastructure;

import com.dbp.proyectobackendmarketexchange.item.domain.ItemImage;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    List<ItemImage> findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(Long itemId);
    long countByItemId(Long itemId);
    Optional<ItemImage> findFirstByItemIdAndPrimaryImageTrue(Long itemId);
    List<ItemImage> findByStorageProvider(StorageProvider storageProvider);

    @Modifying
    @Query("update ItemImage img set img.primaryImage = false where img.item.id = :itemId")
    void clearPrimaryForItem(@Param("itemId") Long itemId);
}
