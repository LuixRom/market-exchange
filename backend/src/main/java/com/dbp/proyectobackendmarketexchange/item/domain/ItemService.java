package com.dbp.proyectobackendmarketexchange.item.domain;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.auth.utils.EmailNormalizer;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.event.item.ItemCreatedEvent;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemImageResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemModerationHistoryResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemSearchCriteria;
import com.dbp.proyectobackendmarketexchange.item.dto.StorageCleanupResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.FavoriteItemRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemImageRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemModerationHistoryRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.StorageServiceRegistry;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Role;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ItemService {
    private static final String IMAGE_DIRECTORY = "items";
    private static final int MAX_IMAGES_PER_ITEM = 4;
    private static final String ITEM_NOT_FOUND = "Item no encontrado";
    private static final String IMAGE_NOT_FOUND = "Imagen no encontrada";
    private static final String ITEM_PATH_PREFIX = "/item/";

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;
    private final FavoriteItemRepository favoriteItemRepository;
    private final ItemImageRepository itemImageRepository;
    private final ItemModerationHistoryRepository itemModerationHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthorizationUtils authorizationUtils;
    private final StorageServiceRegistry storageServiceRegistry;
    private final Clock clock;

    public ItemService(ApplicationEventPublisher eventPublisher,
                       ItemRepository itemRepository,
                       CategoryRepository categoryRepository,
                       UsuarioRepository usuarioRepository,
                       FavoriteItemRepository favoriteItemRepository,
                       ItemImageRepository itemImageRepository,
                       ItemModerationHistoryRepository itemModerationHistoryRepository,
                       AuthorizationUtils authorizationUtils,
                       StorageServiceRegistry storageServiceRegistry,
                       Clock clock) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
        this.favoriteItemRepository = favoriteItemRepository;
        this.itemImageRepository = itemImageRepository;
        this.itemModerationHistoryRepository = itemModerationHistoryRepository;
        this.authorizationUtils = authorizationUtils;
        this.eventPublisher = eventPublisher;
        this.storageServiceRegistry = storageServiceRegistry;
        this.clock = clock;
    }

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemDto) {
        Usuario user = resolveCurrentUser();
        assertActiveUser(user);
        Category category = categoryRepository.findById(itemDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setCondition(itemDto.getCondition());
        item.setCategory(category);
        item.setUsuario(user);
        item.setStatus(ItemStatus.PENDING_REVIEW);

        Item savedItem = itemRepository.save(item);

        storeInitialImages(savedItem, itemDto);

        eventPublisher.publishEvent(new ItemCreatedEvent(this, savedItem));
        return mapItemToDto(savedItem);
    }

    public Page<ItemResponseDto> searchItems(ItemSearchCriteria criteria, Pageable pageable) {
        Usuario current = resolveCurrentUserOrNull();
        return itemRepository.findAll(buildCatalogSpecification(criteria, current), pageable)
                .map(item -> mapItemToDto(item, current));
    }

    @Transactional
    public ItemResponseDto replaceItemImage(Long itemId, MultipartFile image) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        if (!authorizationUtils.isAdminOrResourceOwner(item.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para reemplazar la imagen de este item.");
        }

        if (item.getStatus() == ItemStatus.RESERVED || item.getStatus() == ItemStatus.EXCHANGED) {
            throw new IllegalStateException("No se puede modificar la imagen de un item " + item.getStatus());
        }

        List<ItemImage> existingImages = itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(itemId);
        StorageObject stored = storageServiceRegistry.getDefault().store(image, IMAGE_DIRECTORY);

        try {
            for (ItemImage existingImage : existingImages) {
                itemImageRepository.delete(existingImage);
            }
            itemImageRepository.flush();

            ItemImage newImage = new ItemImage();
            newImage.setItem(item);
            newImage.setStorageKey(stored.storageKey());
            newImage.setStorageProvider(stored.provider());
            newImage.setPrimaryImage(true);
            newImage.setSortOrder(0);
            itemImageRepository.save(newImage);

            item.setImageKey(stored.storageKey());
            item.setImageProvider(stored.provider());
            itemRepository.save(item);
        } catch (RuntimeException e) {
            storageServiceRegistry.getDefault().delete(stored.storageKey());
            throw e;
        }

        for (ItemImage existingImage : existingImages) {
            deleteStoredImageBestEffort(existingImage);
        }

        return mapItemToDto(item);
    }

    public ItemImageResponseDto addItemImage(Long itemId, MultipartFile image) {
        Item item = loadOwnedEditableItem(itemId);
        long currentCount = itemImageRepository.countByItemId(itemId);
        if (currentCount >= MAX_IMAGES_PER_ITEM) {
            throw new IllegalStateException("Un item puede tener como maximo " + MAX_IMAGES_PER_ITEM + " imagenes");
        }

        StorageObject stored = storageServiceRegistry.getDefault().store(image, IMAGE_DIRECTORY);
        try {
            ItemImage itemImage = new ItemImage();
            itemImage.setItem(item);
            itemImage.setStorageKey(stored.storageKey());
            itemImage.setStorageProvider(stored.provider());
            itemImage.setPrimaryImage(currentCount == 0);
            itemImage.setSortOrder((int) currentCount);
            itemImage = itemImageRepository.save(itemImage);

            if (currentCount == 0) {
                item.setImageKey(stored.storageKey());
                item.setImageProvider(stored.provider());
                itemRepository.save(item);
            }

            return mapImageToDto(itemImage);
        } catch (RuntimeException e) {
            storageServiceRegistry.getDefault().delete(stored.storageKey());
            throw e;
        }
    }

    public List<ItemImageResponseDto> getItemImages(Long itemId) {
        itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        return itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(itemId).stream()
                .map(this::mapImageToDto)
                .toList();
    }

    @Transactional
    public void deleteItemImage(Long itemId, Long imageId) {
        Item item = loadOwnedEditableItem(itemId);
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_NOT_FOUND));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND);
        }
        if (itemImageRepository.countByItemId(itemId) <= 1) {
            throw new IllegalStateException("El item debe conservar al menos una imagen");
        }

        boolean wasPrimary = image.isPrimaryImage();
        itemImageRepository.delete(image);
        itemImageRepository.flush();
        deleteStoredImageBestEffort(image);

        if (wasPrimary) {
            ItemImage nextPrimary = itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(itemId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(IMAGE_NOT_FOUND));
            setPrimaryImage(item, nextPrimary);
        }
    }

    @Transactional
    public ItemImageResponseDto markPrimaryImage(Long itemId, Long imageId) {
        Item item = loadOwnedEditableItem(itemId);
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_NOT_FOUND));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND);
        }
        setPrimaryImage(item, image);
        return mapImageToDto(image);
    }

    public byte[] retrieveItemImage(Long itemId, Long imageId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        assertVisible(item);
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_NOT_FOUND));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND);
        }
        return storageServiceRegistry.forProvider(image.getStorageProvider()).retrieve(image.getStorageKey());
    }

    public ItemImage getPrimaryImageOrLegacy(Item item) {
        return itemImageRepository.findFirstByItemIdAndPrimaryImageTrue(item.getId()).orElse(null);
    }

    public ItemImage getVisibleItemImage(Long itemId, Long imageId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        assertVisible(item);
        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_NOT_FOUND));
        if (!image.getItem().getId().equals(itemId)) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND);
        }
        return image;
    }

    public ItemResponseDto approveItem(Long itemId, Boolean approve, String reason) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        Usuario moderator = resolveCurrentUser();
        ItemStatus previousStatus = item.getStatus();
        boolean isApproved = Boolean.TRUE.equals(approve);
        ItemStatus newStatus = isApproved ? ItemStatus.APPROVED : ItemStatus.REJECTED;

        item.setStatus(newStatus);
        item.setRejectionReason(isApproved ? null : normalizeOptional(reason));
        item.setModeratedBy(moderator);
        item.setModeratedAt(java.time.LocalDateTime.now(clock));
        itemRepository.save(item);

        ItemModerationHistory history = new ItemModerationHistory();
        history.setItem(item);
        history.setModerator(moderator);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setReason(normalizeOptional(reason));
        itemModerationHistoryRepository.save(history);

        return mapItemToDto(item);
    }

    public ItemResponseDto approveItem(Long itemId, Boolean approve) {
        return approveItem(itemId, approve, null);
    }

    public List<ItemModerationHistoryResponseDto> getModerationHistory(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException(ITEM_NOT_FOUND);
        }
        return itemModerationHistoryRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::mapModerationHistoryToDto)
                .toList();
    }

    public ItemResponseDto updateItem(Long itemId, ItemRequestDto itemRequestDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        Category category = categoryRepository.findById(itemRequestDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        if (!authorizationUtils.isAdminOrResourceOwner(existingItem.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para actualizar este item.");
        }

        existingItem.setName(itemRequestDto.getName());
        existingItem.setDescription(itemRequestDto.getDescription());
        existingItem.setCondition(itemRequestDto.getCondition());
        existingItem.setCategory(category);

        return mapItemToDto(itemRepository.save(existingItem));
    }

    public ItemResponseDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        assertVisible(item);
        return mapItemToDto(item);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!authorizationUtils.isAdminOrResourceOwner(item.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para eliminar este item.");
        }

        List<ItemImage> images = itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(itemId);
        itemRepository.delete(item);
        for (ItemImage image : images) {
            deleteStoredImageBestEffort(image);
        }
    }

    public List<ItemResponseDto> getAllItems() {
        Usuario current = resolveCurrentUserOrNull();
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        return itemRepository.findAll(buildCatalogSpecification(criteria, current)).stream()
                .map(item -> mapItemToDto(item, current))
                .toList();
    }

    public List<ItemResponseDto> getItemsByUser(Long userId) {
        if (!usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Usuario current = resolveCurrentUserOrNull();
        boolean canSeeAll = current != null && (current.getRole() == Role.ADMIN || current.getId().equals(userId));
        List<Item> items = canSeeAll
                ? itemRepository.findByUsuarioId(userId)
                : itemRepository.findByUsuarioIdAndStatus(userId, ItemStatus.APPROVED);

        return items.stream().map(item -> mapItemToDto(item, current)).toList();
    }

    public List<ItemResponseDto> getItemsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoria no encontrada");
        }

        Usuario current = resolveCurrentUserOrNull();
        List<Item> items = isAdmin(current)
                ? itemRepository.findByCategoryId(categoryId)
                : itemRepository.findByCategoryIdAndStatus(categoryId, ItemStatus.APPROVED);

        return items.stream().map(item -> mapItemToDto(item, current)).toList();
    }

    public List<ItemResponseDto> getUserItems() {
        Usuario usuario = resolveCurrentUser();
        return itemRepository.findByUsuarioId(usuario.getId()).stream()
                .map(item -> mapItemToDto(item, usuario))
                .toList();
    }

    public ItemResponseDto addFavorite(Long itemId) {
        Usuario usuario = resolveCurrentUser();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        assertVisible(item);

        if (!favoriteItemRepository.existsByUsuarioIdAndItemId(usuario.getId(), itemId)) {
            FavoriteItem favorite = new FavoriteItem();
            favorite.setUsuario(usuario);
            favorite.setItem(item);
            favoriteItemRepository.save(favorite);
        }

        return mapItemToDto(item, usuario);
    }

    public void removeFavorite(Long itemId) {
        Usuario usuario = resolveCurrentUser();
        favoriteItemRepository.findByUsuarioIdAndItemId(usuario.getId(), itemId)
                .ifPresent(favoriteItemRepository::delete);
    }

    public List<ItemResponseDto> getFavorites() {
        Usuario usuario = resolveCurrentUser();
        return favoriteItemRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.getId()).stream()
                .map(favorite -> mapItemToDto(favorite.getItem(), usuario))
                .toList();
    }

    public StorageCleanupResponseDto cleanupOrphanLocalItemImages() {
        List<String> storedKeys = storageServiceRegistry.forProvider(StorageProvider.LOCAL).listKeys(IMAGE_DIRECTORY);
        Set<String> referencedKeys = new HashSet<>();
        itemImageRepository.findByStorageProvider(StorageProvider.LOCAL)
                .forEach(image -> referencedKeys.add(image.getStorageKey()));
        itemRepository.findAll().forEach(item -> {
            if (item.getImageProvider() == StorageProvider.LOCAL && item.getImageKey() != null) {
                referencedKeys.add(item.getImageKey());
            }
        });

        List<String> deleted = new ArrayList<>();
        for (String storedKey : storedKeys) {
            if (!referencedKeys.contains(storedKey)) {
                storageServiceRegistry.forProvider(StorageProvider.LOCAL).delete(storedKey);
                deleted.add(storedKey);
            }
        }
        return new StorageCleanupResponseDto(storedKeys.size(), deleted.size(), deleted);
    }

    private Specification<Item> buildCatalogSpecification(ItemSearchCriteria criteria, Usuario current) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!isAdmin(current)) {
                predicates.add(builder.equal(root.get("status"), ItemStatus.APPROVED));
            } else if (criteria.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getCategoryId() != null) {
                predicates.add(builder.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }
            if (criteria.getUserId() != null) {
                predicates.add(builder.equal(root.get("usuario").get("id"), criteria.getUserId()));
            }
            if (criteria.getCondition() != null) {
                predicates.add(builder.equal(root.get("condition"), criteria.getCondition()));
            }
            if (criteria.getQ() != null && !criteria.getQ().isBlank()) {
                String q = "%" + criteria.getQ().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), q),
                        builder.like(builder.lower(root.get("description")), q)
                ));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void assertVisible(Item item) {
        Usuario current = resolveCurrentUserOrNull();
        if (isAdmin(current) || (current != null && item.getUsuario().getId().equals(current.getId()))) {
            return;
        }
        if (item.getStatus() != ItemStatus.APPROVED) {
            throw new ResourceNotFoundException(ITEM_NOT_FOUND);
        }
    }

    private Usuario resolveCurrentUser() {
        Usuario usuario = resolveCurrentUserOrNull();
        if (usuario == null) {
            throw new ForbiddenOperationException("Usuario no autenticado");
        }
        return usuario;
    }

    private void storeInitialImages(Item item, ItemRequestDto itemDto) {
        List<MultipartFile> images = new ArrayList<>();
        if (itemDto.getImages() != null) {
            images.addAll(itemDto.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList());
        }
        if (images.isEmpty() && itemDto.getImage() != null && !itemDto.getImage().isEmpty()) {
            images.add(itemDto.getImage());
        }
        if (images.size() > MAX_IMAGES_PER_ITEM) {
            throw new IllegalStateException("Un item puede tener como maximo " + MAX_IMAGES_PER_ITEM + " imagenes");
        }

        List<ItemImage> savedImages = new ArrayList<>();
        try {
            for (int index = 0; index < images.size(); index++) {
                StorageObject stored = storageServiceRegistry.getDefault().store(images.get(index), IMAGE_DIRECTORY);
                ItemImage itemImage = new ItemImage();
                itemImage.setItem(item);
                itemImage.setStorageKey(stored.storageKey());
                itemImage.setStorageProvider(stored.provider());
                itemImage.setPrimaryImage(index == 0);
                itemImage.setSortOrder(index);
                savedImages.add(itemImageRepository.save(itemImage));
            }
            if (!savedImages.isEmpty()) {
                ItemImage primary = savedImages.get(0);
                item.setImageKey(primary.getStorageKey());
                item.setImageProvider(primary.getStorageProvider());
                itemRepository.save(item);
            }
        } catch (RuntimeException e) {
            savedImages.forEach(this::deleteStoredImageBestEffort);
            throw e;
        }
    }

    private Item loadOwnedEditableItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
        if (!authorizationUtils.isAdminOrResourceOwner(item.getUsuario().getId())) {
            throw new ForbiddenOperationException("No tienes permiso para modificar imagenes de este item.");
        }
        if (item.getStatus() == ItemStatus.RESERVED || item.getStatus() == ItemStatus.EXCHANGED) {
            throw new IllegalStateException("No se pueden modificar imagenes de un item " + item.getStatus());
        }
        return item;
    }

    private void setPrimaryImage(Item item, ItemImage image) {
        itemImageRepository.clearPrimaryForItem(item.getId());
        image.setPrimaryImage(true);
        itemImageRepository.save(image);
        item.setImageKey(image.getStorageKey());
        item.setImageProvider(image.getStorageProvider());
        itemRepository.save(item);
    }

    private void deleteStoredImageBestEffort(ItemImage image) {
        try {
            storageServiceRegistry.forProvider(image.getStorageProvider()).delete(image.getStorageKey());
        } catch (RuntimeException ignored) {
            // Best-effort cleanup: the DB record is already gone, so a leftover blob is not fatal.
        }
    }

    private Usuario resolveCurrentUserOrNull() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return null;
        }
        String email = ((UserDetails) principal).getUsername();
        return usuarioRepository.findByEmail(EmailNormalizer.normalize(email)).orElse(null);
    }

    private boolean isAdmin(Usuario usuario) {
        return usuario != null && usuario.getRole() == Role.ADMIN;
    }

    private void assertActiveUser(Usuario usuario) {
        if (usuario.isBlocked() || usuario.isSuspended()) {
            throw new ForbiddenOperationException("Tu cuenta no puede publicar items en este momento");
        }
    }

    private ItemResponseDto mapItemToDto(Item item) {
        return mapItemToDto(item, resolveCurrentUserOrNull());
    }

    private ItemResponseDto mapItemToDto(Item item, Usuario current) {
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(item.getId());
        responseDto.setName(item.getName());
        responseDto.setDescription(item.getDescription());
        responseDto.setCondition(item.getCondition());
        responseDto.setStatus(item.getStatus());
        responseDto.setCreatedAt(item.getCreatedAt());
        responseDto.setRejectionReason(item.getRejectionReason());
        responseDto.setModeratedAt(item.getModeratedAt());
        if (item.getModeratedBy() != null) {
            responseDto.setModeratedById(item.getModeratedBy().getId());
            responseDto.setModeratedByEmail(item.getModeratedBy().getEmail());
        }

        if (item.getUsuario() != null) {
            responseDto.setUserId(item.getUsuario().getId());
            responseDto.setUserName(item.getUsuario().getEmail());
        }
        if (item.getCategory() != null) {
            responseDto.setCategoryId(item.getCategory().getId());
            responseDto.setCategoryName(item.getCategory().getName());
        }
        if (item.getImageKey() != null) {
            responseDto.setImageUrl(ITEM_PATH_PREFIX + item.getId() + "/image");
        }
        List<ItemImage> images = itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(item.getId());
        if (!images.isEmpty()) {
            responseDto.setImageUrl(buildItemImageUrl(item.getId(), images.get(0).getId()));
            responseDto.setImageUrls(images.stream()
                    .map(image -> buildItemImageUrl(item.getId(), image.getId()))
                    .toList());
        }
        if (current != null) {
            responseDto.setFavorite(favoriteItemRepository.existsByUsuarioIdAndItemId(current.getId(), item.getId()));
        }

        return responseDto;
    }

    private ItemModerationHistoryResponseDto mapModerationHistoryToDto(ItemModerationHistory history) {
        ItemModerationHistoryResponseDto dto = new ItemModerationHistoryResponseDto();
        dto.setId(history.getId());
        dto.setItemId(history.getItem().getId());
        dto.setModeratorId(history.getModerator().getId());
        dto.setModeratorEmail(history.getModerator().getEmail());
        dto.setPreviousStatus(history.getPreviousStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setReason(history.getReason());
        dto.setCreatedAt(history.getCreatedAt());
        return dto;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String buildItemImageUrl(Long itemId, Long imageId) {
        return ITEM_PATH_PREFIX + itemId + "/images/" + imageId;
    }

    private ItemImageResponseDto mapImageToDto(ItemImage image) {
        ItemImageResponseDto dto = new ItemImageResponseDto();
        dto.setId(image.getId());
        dto.setItemId(image.getItem().getId());
        dto.setImageUrl(buildItemImageUrl(image.getItem().getId(), image.getId()));
        dto.setPrimary(image.isPrimaryImage());
        dto.setSortOrder(image.getSortOrder());
        dto.setCreatedAt(image.getCreatedAt());
        return dto;
    }
}
