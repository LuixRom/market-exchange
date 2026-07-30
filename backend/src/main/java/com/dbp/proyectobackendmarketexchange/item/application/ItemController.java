package com.dbp.proyectobackendmarketexchange.item.application;


import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemImage;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemImageResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemModerationHistoryResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemSearchCriteria;
import com.dbp.proyectobackendmarketexchange.item.dto.StorageCleanupResponseDto;
import com.dbp.proyectobackendmarketexchange.item.domain.Condition;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.StorageServiceRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final StorageServiceRegistry storageServiceRegistry;

    public ItemController(ItemService itemService, ItemRepository itemRepository, StorageServiceRegistry storageServiceRegistry) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
        this.storageServiceRegistry = storageServiceRegistry;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> createItem(@ModelAttribute ItemRequestDto requestDto) {
        ItemResponseDto responseDto = itemService.createItem(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{itemId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        ItemImage primaryImage = itemService.getPrimaryImageOrLegacy(item);
        if (primaryImage != null) {
            StorageService storageService = storageServiceRegistry.forProvider(primaryImage.getStorageProvider());
            if (!storageService.exists(primaryImage.getStorageKey())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeTypeFor(primaryImage.getStorageKey())))
                    .body(storageService.retrieve(primaryImage.getStorageKey()));
        }

        if (item.getImageKey() == null || item.getImageProvider() == null) {
            return ResponseEntity.notFound().build();
        }

        StorageService storageService = storageServiceRegistry.forProvider(item.getImageProvider());
        if (!storageService.exists(item.getImageKey())) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileContent = storageService.retrieve(item.getImageKey());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeTypeFor(item.getImageKey())))
                .body(fileContent);
    }

    @GetMapping("/{itemId}/images")
    public ResponseEntity<List<ItemImageResponseDto>> getItemImages(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getItemImages(itemId));
    }

    @GetMapping("/{itemId}/images/{imageId}")
    public ResponseEntity<byte[]> getItemImage(@PathVariable Long itemId, @PathVariable Long imageId) {
        ItemImage image = itemService.getVisibleItemImage(itemId, imageId);
        StorageService storageService = storageServiceRegistry.forProvider(image.getStorageProvider());
        if (!storageService.exists(image.getStorageKey())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeTypeFor(image.getStorageKey())))
                .body(storageService.retrieve(image.getStorageKey()));
    }

    @PutMapping(value = "/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> replaceItemImage(@PathVariable Long itemId, @RequestParam("image") MultipartFile image) {
        ItemResponseDto responseDto = itemService.replaceItemImage(itemId, image);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(value = "/{itemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemImageResponseDto> addItemImage(@PathVariable Long itemId, @RequestParam("image") MultipartFile image) {
        return new ResponseEntity<>(itemService.addItemImage(itemId, image), HttpStatus.CREATED);
    }

    @DeleteMapping("/{itemId}/images/{imageId}")
    public ResponseEntity<Void> deleteItemImage(@PathVariable Long itemId, @PathVariable Long imageId) {
        itemService.deleteItemImage(itemId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/images/{imageId}/primary")
    public ResponseEntity<ItemImageResponseDto> markPrimaryImage(@PathVariable Long itemId, @PathVariable Long imageId) {
        return ResponseEntity.ok(itemService.markPrimaryImage(itemId, imageId));
    }



    @PostMapping("/{itemId}/approve")
    public ResponseEntity<ItemResponseDto> approveItem(@PathVariable Long itemId,
                                                       @RequestParam boolean approve,
                                                       @RequestParam(required = false) String reason) {
        ItemResponseDto responseDto = itemService.approveItem(itemId, approve, reason);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{itemId}/moderation-history")
    public ResponseEntity<List<ItemModerationHistoryResponseDto>> getModerationHistory(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.getModerationHistory(itemId));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long itemId, @RequestBody ItemRequestDto requestDto) {
        ItemResponseDto updateItem = itemService.updateItem(itemId, requestDto);
        return ResponseEntity.ok(updateItem);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long itemId) {
        ItemResponseDto item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/catalog")
    public ResponseEntity<Page<ItemResponseDto>> searchItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Condition condition,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        criteria.setCategoryId(categoryId);
        criteria.setUserId(userId);
        criteria.setCondition(condition);
        criteria.setStatus(status);
        criteria.setQ(q);
        return ResponseEntity.ok(itemService.searchItems(criteria, pageable));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByCategory(@PathVariable Long categoryId) {
        List<ItemResponseDto> items = itemService.getItemsByCategory(categoryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ItemResponseDto>> getItemByUserId(@PathVariable Long userId) {
        List<ItemResponseDto> items = itemService.getItemsByUser(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ItemResponseDto>> getUserItems() {
        List<ItemResponseDto> items = itemService.getUserItems();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/favorite")
    public ResponseEntity<ItemResponseDto> addFavorite(@PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.addFavorite(itemId));
    }

    @DeleteMapping("/{itemId}/favorite")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long itemId) {
        itemService.removeFavorite(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ItemResponseDto>> getFavorites() {
        return ResponseEntity.ok(itemService.getFavorites());
    }

    @DeleteMapping("/images/orphans")
    public ResponseEntity<StorageCleanupResponseDto> cleanupOrphanImages() {
        return ResponseEntity.ok(itemService.cleanupOrphanLocalItemImages());
    }

    // Las keys siempre las genera StorageService con una de estas extensiones (o son
    // archivos legacy .jpg copiados manualmente) — no hace falta tocar el filesystem para
    // saber el MIME type de servirlas.
    private String mimeTypeFor(String storageKey) {
        String lower = storageKey.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
