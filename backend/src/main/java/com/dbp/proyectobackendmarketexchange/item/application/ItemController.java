package com.dbp.proyectobackendmarketexchange.item.application;


import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.dbp.proyectobackendmarketexchange.item.domain.ItemService.IMAGE_UPLOAD_DIR;

@RestController
@RequestMapping("/item")
public class ItemController {
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    public ItemController(ItemService itemService, ItemRepository itemRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
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

        if (item.getImagePath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Construir la ruta del archivo
            Path imagePath = Paths.get(IMAGE_UPLOAD_DIR, item.getImagePath());

            // Verificar si el archivo existe
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            // Leer el contenido del archivo
            byte[] fileContent = Files.readAllBytes(imagePath);

            // Determinar el tipo MIME
            String mimeType = Files.probeContentType(imagePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream"))
                    .body(fileContent);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
}