package com.dbp.proyectobackendmarketexchange.item;

import com.dbp.proyectobackendmarketexchange.auth.utils.AuthorizationUtils;
import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.event.item.ItemCreatedEvent;
import com.dbp.proyectobackendmarketexchange.exception.ForbiddenOperationException;
import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemImage;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemStatus;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.FavoriteItemRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemImageRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemModerationHistoryRepository;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageObject;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageProvider;
import com.dbp.proyectobackendmarketexchange.storage.domain.StorageService;
import com.dbp.proyectobackendmarketexchange.storage.infrastructure.StorageServiceRegistry;
import com.dbp.proyectobackendmarketexchange.usuario.domain.Usuario;
import com.dbp.proyectobackendmarketexchange.usuario.infrastructure.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FavoriteItemRepository favoriteItemRepository;

    @Mock
    private ItemImageRepository itemImageRepository;

    @Mock
    private ItemModerationHistoryRepository itemModerationHistoryRepository;

    @Mock
    private AuthorizationUtils authorizationUtils;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StorageServiceRegistry storageServiceRegistry;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(anyLong())).thenReturn(List.of());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateItem() {
        // Datos de entrada
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCategory_id(1L);
        requestDto.setUser_id(1L);

        Category category = new Category();
        category.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList()));

        Item savedItem = new Item();
        savedItem.setId(1L);

        // Simulaciones
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        // Ejecutar el método
        ItemResponseDto result = itemService.createItem(requestDto);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(eventPublisher, times(1)).publishEvent(any(ItemCreatedEvent.class));
        verify(storageServiceRegistry, never()).getDefault();
    }

    @Test
    public void testCreateItem_WithImage_StoresAndSetsKey() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCategory_id(1L);
        requestDto.setUser_id(1L);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        requestDto.setImage(image);

        Category category = new Category();
        category.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList()));

        Item savedItem = new Item();
        savedItem.setId(1L);

        StorageObject storageObject = new StorageObject("items/abc.jpg", StorageProvider.LOCAL, "image/jpeg", "foto.jpg", 123L);

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);
        when(storageServiceRegistry.getDefault()).thenReturn(storageService);
        when(storageService.store(eq(image), anyString())).thenReturn(storageObject);
        when(itemImageRepository.save(any(ItemImage.class))).thenAnswer(invocation -> {
            ItemImage itemImage = invocation.getArgument(0);
            itemImage.setId(99L);
            return itemImage;
        });
        when(itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(1L))
                .thenReturn(List.of(buildItemImage(savedItem, "items/abc.jpg")));

        ItemResponseDto result = itemService.createItem(requestDto);

        assertNotNull(result);
        assertEquals("/item/1/images/77", result.getImageUrl());
        assertEquals("items/abc.jpg", savedItem.getImageKey());
        assertEquals(StorageProvider.LOCAL, savedItem.getImageProvider());
        verify(itemRepository, times(2)).save(any(Item.class)); // una vez para obtener el id, otra con la key
    }

    @Test
    public void testUpdateItem() {
        // Datos de entrada
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setCategory_id(1L);
        requestDto.setUser_id(1L);

        Category category = new Category();
        category.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setUsuario(usuario);

        // Simulaciones
        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        // Ejecutar el metodo
        ItemResponseDto result = itemService.updateItem(1L, requestDto);

        // Verificaciones
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void testDeleteItem() {
        // Datos de entrada
        Item item = new Item();
        item.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        item.setUsuario(usuario);

        // Simulaciones
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        // Ejecutar el metodo
        itemService.deleteItem(1L);

        // Verificaciones
        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    public void testGetItemById() {
        // Preparación de datos
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@example.com");

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Item item = new Item();
        item.setId(1L);
        item.setUsuario(usuario);
        item.setCategory(category);
        item.setStatus(ItemStatus.APPROVED);

        // Simulación de comportamiento
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Ejecución del metodo
        ItemResponseDto result = itemService.getItemById(1L);

        // Verificación
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getUserName());
        verify(itemRepository, times(1)).findById(1L);
    }


    @Test
    public void testGetAllItems() {
        // Preparación de datos
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setCategory(category);
        item.setUsuario(usuario);
        item.setStatus(ItemStatus.APPROVED);

        List<Item> items = List.of(item);

        // Simulación de comportamiento
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(items);

        // Ejecución del metodo
        List<ItemResponseDto> result = itemService.getAllItems();

        // Verificación
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategoryName());
        assertEquals("usuario@example.com", result.get(0).getUserName());
    }


    @Test
    public void testDeleteItemUnauthorized() {
        // Datos de entrada
        Item item = new Item();
        item.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        item.setUsuario(usuario);

        // Simulaciones
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(false);

        // Ejecutar el metodo y verificación de excepción
        assertThrows(ForbiddenOperationException.class, () -> {
            itemService.deleteItem(1L);
        });

        verify(itemRepository, never()).delete(any(Item.class));
    }

    // ---- replaceItemImage: reemplazo y compensación ----

    private Item buildItemWithImage(Long id, Usuario owner, ItemStatus status, String existingKey) {
        Item item = new Item();
        item.setId(id);
        item.setUsuario(owner);
        item.setStatus(status);
        item.setImageKey(existingKey);
        item.setImageProvider(existingKey != null ? StorageProvider.LOCAL : null);
        return item;
    }

    private ItemImage buildItemImage(Item item, String storageKey) {
        ItemImage image = new ItemImage();
        image.setId(77L);
        image.setItem(item);
        image.setStorageKey(storageKey);
        image.setStorageProvider(StorageProvider.LOCAL);
        image.setPrimaryImage(true);
        return image;
    }

    @Test
    public void testReplaceItemImage_Success_StoresNewThenDeletesOld() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.APPROVED, "items/old.jpg");
        MultipartFile newImage = mock(MultipartFile.class);
        StorageObject newObject = new StorageObject("items/new.jpg", StorageProvider.LOCAL, "image/jpeg", "foto.jpg", 10L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(10L))
                .thenReturn(List.of(buildItemImage(item, "items/old.jpg")));
        when(storageServiceRegistry.getDefault()).thenReturn(storageService);
        when(storageServiceRegistry.forProvider(StorageProvider.LOCAL)).thenReturn(storageService);
        when(storageService.store(eq(newImage), anyString())).thenReturn(newObject);
        when(itemImageRepository.save(any(ItemImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(item)).thenReturn(item);

        ItemResponseDto result = itemService.replaceItemImage(10L, newImage);

        assertNotNull(result);
        assertEquals("items/new.jpg", item.getImageKey());

        InOrder inOrder = inOrder(storageService, itemRepository);
        inOrder.verify(storageService).store(eq(newImage), anyString());
        inOrder.verify(itemRepository).save(item);
        inOrder.verify(storageService).delete("items/old.jpg");
    }

    @Test
    public void testReplaceItemImage_SaveFails_CompensatesByDeletingNewFile() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.APPROVED, "items/old.jpg");
        MultipartFile newImage = mock(MultipartFile.class);
        StorageObject newObject = new StorageObject("items/new.jpg", StorageProvider.LOCAL, "image/jpeg", "foto.jpg", 10L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(storageServiceRegistry.getDefault()).thenReturn(storageService);
        when(storageService.store(eq(newImage), anyString())).thenReturn(newObject);
        when(itemImageRepository.save(any(ItemImage.class))).thenThrow(new RuntimeException("DB caida"));
        when(itemRepository.save(item)).thenThrow(new RuntimeException("DB caída"));

        assertThrows(RuntimeException.class, () -> itemService.replaceItemImage(10L, newImage));

        verify(storageService, times(1)).delete("items/new.jpg");
        verify(storageService, never()).delete("items/old.jpg");
        assertEquals("items/old.jpg", item.getImageKey(), "el Item en memoria no debe quedar con la key nueva si el save falló");
    }

    @Test
    public void testReplaceItemImage_StoreFails_NeverTouchesDbOrOldFile() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.APPROVED, "items/old.jpg");
        MultipartFile newImage = mock(MultipartFile.class);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(storageServiceRegistry.getDefault()).thenReturn(storageService);
        when(storageService.store(eq(newImage), anyString())).thenThrow(new RuntimeException("Storage caído"));

        assertThrows(RuntimeException.class, () -> itemService.replaceItemImage(10L, newImage));

        verify(itemRepository, never()).save(any());
        verify(storageService, never()).delete(anyString());
    }

    @Test
    public void testReplaceItemImage_OldDeleteFails_StillSucceeds() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.APPROVED, "items/old.jpg");
        MultipartFile newImage = mock(MultipartFile.class);
        StorageObject newObject = new StorageObject("items/new.jpg", StorageProvider.LOCAL, "image/jpeg", "foto.jpg", 10L);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);
        when(itemImageRepository.findByItemIdOrderByPrimaryImageDescSortOrderAscIdAsc(10L))
                .thenReturn(List.of(buildItemImage(item, "items/old.jpg")));
        when(storageServiceRegistry.getDefault()).thenReturn(storageService);
        when(storageServiceRegistry.forProvider(StorageProvider.LOCAL)).thenReturn(storageService);
        when(storageService.store(eq(newImage), anyString())).thenReturn(newObject);
        when(itemImageRepository.save(any(ItemImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(item)).thenReturn(item);
        doThrow(new RuntimeException("no se pudo borrar")).when(storageService).delete("items/old.jpg");

        ItemResponseDto result = assertDoesNotThrow(() -> itemService.replaceItemImage(10L, newImage));

        assertNotNull(result);
        assertEquals("items/new.jpg", item.getImageKey());
    }

    @Test
    public void testReplaceItemImage_ForbiddenWhenNotOwner() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.APPROVED, "items/old.jpg");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () -> itemService.replaceItemImage(10L, mock(MultipartFile.class)));
        verify(storageServiceRegistry, never()).getDefault();
    }

    @Test
    public void testReplaceItemImage_BlockedWhenReserved() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.RESERVED, "items/old.jpg");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> itemService.replaceItemImage(10L, mock(MultipartFile.class)));
        verify(storageServiceRegistry, never()).getDefault();
    }

    @Test
    public void testReplaceItemImage_BlockedWhenExchanged() {
        Usuario owner = new Usuario();
        owner.setId(1L);
        Item item = buildItemWithImage(10L, owner, ItemStatus.EXCHANGED, "items/old.jpg");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(authorizationUtils.isAdminOrResourceOwner(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> itemService.replaceItemImage(10L, mock(MultipartFile.class)));
    }
}
