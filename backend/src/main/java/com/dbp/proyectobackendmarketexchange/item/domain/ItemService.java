package com.dbp.proyectobackendmarketexchange.item.domain;


import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.event.item.ItemCreatedEvent;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.StorageServiceRegistry;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;
import java.util.List;


@Service
public class ItemService {
    private static final String IMAGE_DIRECTORY = "items";

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthorizationUtils authorizationUtils;
    private final StorageServiceRegistry storageServiceRegistry;

    public ItemService(ApplicationEventPublisher eventPublisher, ItemRepository itemRepository, CategoryRepository categoryRepository,
                        UsuarioRepository usuarioRepository, AuthorizationUtils authorizationUtils, StorageServiceRegistry storageServiceRegistry) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
        this.authorizationUtils = authorizationUtils;
        this.eventPublisher = eventPublisher;
        this.storageServiceRegistry = storageServiceRegistry;
    }

    public ItemResponseDto createItem(ItemRequestDto itemDto) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }

        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Buscar la categoría
        Category category = categoryRepository.findById(itemDto.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Mapear el DTO a la entidad Item
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setCondition(itemDto.getCondition());
        item.setCategory(category);
        item.setUsuario(user);
        item.setStatus(ItemStatus.PENDING_REVIEW);

        // Guardar el ítem primero para obtener su ID
        Item savedItem = itemRepository.save(item);

        // Manejar la imagen
        if (itemDto.getImage() != null && !itemDto.getImage().isEmpty()) {
            StorageObject stored = storageServiceRegistry.getDefault().store(itemDto.getImage(), IMAGE_DIRECTORY);
            savedItem.setImageKey(stored.storageKey());
            savedItem.setImageProvider(stored.provider());
            itemRepository.save(savedItem);
        }

        eventPublisher.publishEvent(new ItemCreatedEvent(this, savedItem));

        // Mapear a Response DTO
        return mapItemToDto(savedItem);
    }

    /**
     * Reemplaza la imagen principal del item. Orden deliberado (sin @Transactional, igual
     * que el resto de esta clase): guardar el archivo NUEVO primero, actualizar la fila en
     * base, y recién ahí borrar el archivo VIEJO -así el Item nunca queda apuntando a un
     * archivo inexistente, incluso si algún paso falla a mitad de camino-.
     */
    public ItemResponseDto replaceItemImage(Long itemId, MultipartFile image) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        if (!authorizationUtils.isAdminOrResourceOwner(item.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para reemplazar la imagen de este ítem.");
        }

        if (item.getStatus() == ItemStatus.RESERVED || item.getStatus() == ItemStatus.EXCHANGED) {
            throw new IllegalStateException("No se puede modificar la imagen de un ítem " + item.getStatus());
        }

        String previousKey = item.getImageKey();
        StorageProvider previousProvider = item.getImageProvider();

        StorageObject stored = storageServiceRegistry.getDefault().store(image, IMAGE_DIRECTORY);

        try {
            item.setImageKey(stored.storageKey());
            item.setImageProvider(stored.provider());
            itemRepository.save(item);
        } catch (RuntimeException e) {
            // Compensación: la fila no se pudo actualizar, así que el archivo nuevo queda
            // huérfano -lo borramos y revertimos el Item en memoria para que siga
            // apuntando al archivo viejo, que nunca se tocó-.
            item.setImageKey(previousKey);
            item.setImageProvider(previousProvider);
            storageServiceRegistry.getDefault().delete(stored.storageKey());
            throw e;
        }

        if (previousKey != null) {
            try {
                // Best-effort: si falla el borrado del archivo viejo, se acepta un huérfano
                // en storage -el Item ya quedó consistente apuntando al archivo nuevo-.
                storageServiceRegistry.getDefault().delete(previousKey);
            } catch (RuntimeException e) {
                // no-op: el reemplazo ya se completó con éxito, un huérfano en storage no
                // debe hacer fallar la operación completa.
            }
        }

        return mapItemToDto(item);
    }

    public ItemResponseDto approveItem(Long itemId, Boolean approve) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (approve) {
            item.setStatus(ItemStatus.APPROVED);
        } else {
            item.setStatus(ItemStatus.REJECTED);
        }

        itemRepository.save(item);

        return mapItemToDto(item);
    }

    public ItemResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        Category category = categoryRepository.findById(itemRequestDto.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (!authorizationUtils.isAdminOrResourceOwner(existingItem.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para actualizar este ítem.");
        }

        // Actualizar campos
        existingItem.setName(itemRequestDto.getName());
        existingItem.setDescription(itemRequestDto.getDescription());
        existingItem.setCondition(itemRequestDto.getCondition());
        existingItem.setCategory(category);

        Item updatedItem = itemRepository.save(existingItem);

        return mapItemToDto(updatedItem);
    }


    public ItemResponseDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        return mapItemToDto(item);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!authorizationUtils.isAdminOrResourceOwner(item.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para eliminar este ítem.");
        }

        itemRepository.delete(item);
    }

    public List<ItemResponseDto> getAllItems() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }
    public List<ItemResponseDto> getItemsByUser(Long userId) {
        // Verificar si el usuario existe
        if (!usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        // Obtener los ítems del usuario
        List<Item> items = itemRepository.findByUsuarioId(userId);

        return items.stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getItemsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoría no encontrada");
        }
        List<Item> items = itemRepository.findByCategoryId(categoryId);
        return items.stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> getUserItems() {
        // Obtener el email del usuario autenticado usando Spring Security
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }

        // Buscar el usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Obtener los ítems del usuario autenticado
        List<Item> items = itemRepository.findByUsuarioId(usuario.getId());

        return items.stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());
    }

    private ItemResponseDto mapItemToDto(Item item) {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(item.getId());
        responseDto.setName(item.getName());
        responseDto.setDescription(item.getDescription());
        responseDto.setCondition(item.getCondition());
        responseDto.setStatus(item.getStatus());
        responseDto.setCreatedAt(item.getCreatedAt());


        if (item.getUsuario() != null && item.getUsuario().getEmail() != null) {
            responseDto.setUserName(item.getUsuario().getEmail());
        } else {
            responseDto.setUserName("Usuario desconocido");
        }

        if (item.getUsuario() != null && item.getUsuario().getId() != null) {
            responseDto.setUser_id(item.getUsuario().getId());
        } else {
            responseDto.setUserName("Usuario desconocido");
        }


        if (item.getCategory() != null && item.getCategory().getName() != null) {
            responseDto.setCategoryName(item.getCategory().getName());
        } else {
            responseDto.setCategoryName("Categoría desconocida");
        }

        if (item.getImageKey() != null) {
            responseDto.setImageUrl("/item/" + item.getId() + "/image");
        }

        return responseDto;
    }

}
