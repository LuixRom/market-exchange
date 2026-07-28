package com.dbp.proyectobackendmarketexchange.item.application;


import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.StorageServiceRegistry;
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

        if (item.getImageKey() == null) {
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

    @PutMapping(value = "/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> replaceItemImage(@PathVariable Long itemId, @RequestParam("image") MultipartFile image) {
        ItemResponseDto responseDto = itemService.replaceItemImage(itemId, image);
        return ResponseEntity.ok(responseDto);
    }



    @PostMapping("/{itemId}/approve")
    public ResponseEntity<ItemResponseDto> approveItem(@PathVariable Long itemId, @RequestParam boolean approve) {
        ItemResponseDto responseDto = itemService.approveItem(itemId, approve);
        return ResponseEntity.ok(responseDto);
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
