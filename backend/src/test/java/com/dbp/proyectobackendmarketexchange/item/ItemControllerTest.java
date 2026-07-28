package com.dbp.proyectobackendmarketexchange.item;

import com.dbp.proyectobackendmarketexchange.config.JwtService;
import com.dbp.proyectobackendmarketexchange.item.application.ItemController;
import com.dbp.proyectobackendmarketexchange.item.domain.ItemService;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemRequestDto;
import com.dbp.proyectobackendmarketexchange.item.dto.ItemResponseDto;
import com.dbp.proyectobackendmarketexchange.item.infrastructure.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)  // Deshabilitar filtros de seguridad en pruebas
@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtService jwtService;  // Agrega el mock para JwtService

    @MockBean
    private ItemRepository itemRepository;


    @Test
    public void testCreateItem() throws Exception {
        // Preparar datos
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del servicio
        when(itemService.createItem(any(ItemRequestDto.class))).thenReturn(responseDto);

        // Realizar la solicitud POST y verificar la respuesta
        // El controlador consume multipart/form-data (ItemRequestDto tiene un campo MultipartFile)
        mockMvc.perform(multipart("/item")
                        .param("category_id", "1")
                        .param("user_id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).createItem(any(ItemRequestDto.class));
    }

    @Test
    public void testApproveItem() throws Exception {
        // Preparar datos
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del servicio
        when(itemService.approveItem(1L, true)).thenReturn(responseDto);

        // Realizar la solicitud POST y verificar la respuesta
        mockMvc.perform(post("/item/1/approve")
                        .param("approve", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).approveItem(1L, true);
    }

    @Test
    public void testUpdateItem() throws Exception {
        // Preparar datos
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del servicio
        when(itemService.updateItem(eq(1L), any(ItemRequestDto.class))).thenReturn(responseDto);

        // Realizar la solicitud PUT y verificar la respuesta
        mockMvc.perform(put("/item/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category_id\": 1, \"user_id\": 1}"))  // JSON simulado del DTO de request
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).updateItem(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    public void testGetItem() throws Exception {
        // Preparar datos
        ItemResponseDto responseDto = new ItemResponseDto();
        responseDto.setId(1L);

        // Simular comportamiento del servicio
        when(itemService.getItemById(1L)).thenReturn(responseDto);

        // Realizar la solicitud GET y verificar la respuesta
        mockMvc.perform(get("/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).getItemById(1L);
    }

    @Test
    public void testGetAllItems() throws Exception {
        // Preparar datos
        ItemResponseDto item1 = new ItemResponseDto();
        item1.setId(1L);
        ItemResponseDto item2 = new ItemResponseDto();
        item2.setId(2L);
        List<ItemResponseDto> items = Arrays.asList(item1, item2);

        // Simular comportamiento del servicio
        when(itemService.getAllItems()).thenReturn(items);

        // Realizar la solicitud GET y verificar la respuesta
        mockMvc.perform(get("/item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).getAllItems();
    }

    @Test
    public void testDeleteItem() throws Exception {
        // Simular comportamiento del servicio
        doNothing().when(itemService).deleteItem(1L);

        // Realizar la solicitud DELETE y verificar la respuesta
        mockMvc.perform(delete("/item/1"))
                .andExpect(status().isNoContent());

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).deleteItem(1L);
    }

    @Test
    public void testGetItemsByCategory() throws Exception {
        // Preparar datos
        ItemResponseDto item1 = new ItemResponseDto();
        item1.setId(1L);
        ItemResponseDto item2 = new ItemResponseDto();
        item2.setId(2L);
        List<ItemResponseDto> items = Arrays.asList(item1, item2);

        // Simular comportamiento del servicio
        when(itemService.getItemsByCategory(1L)).thenReturn(items);

        // Realizar la solicitud GET y verificar la respuesta
        mockMvc.perform(get("/item/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).getItemsByCategory(1L);
    }

    @Test
    public void testGetItemsByUser() throws Exception {
        // Preparar datos
        ItemResponseDto item1 = new ItemResponseDto();
        item1.setId(1L);
        ItemResponseDto item2 = new ItemResponseDto();
        item2.setId(2L);
        List<ItemResponseDto> items = Arrays.asList(item1, item2);

        // Simular comportamiento del servicio
        when(itemService.getItemsByUser(1L)).thenReturn(items);

        // Realizar la solicitud GET y verificar la respuesta
        mockMvc.perform(get("/item/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).getItemsByUser(1L);
    }

    @Test
    public void testGetUserItems() throws Exception {
        // Preparar datos
        ItemResponseDto item1 = new ItemResponseDto();
        item1.setId(1L);
        ItemResponseDto item2 = new ItemResponseDto();
        item2.setId(2L);
        List<ItemResponseDto> items = Arrays.asList(item1, item2);

        // Simular comportamiento del servicio
        when(itemService.getUserItems()).thenReturn(items);

        // Realizar la solicitud GET y verificar la respuesta
        mockMvc.perform(get("/item/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        // Verificar que el servicio fue llamado
        verify(itemService, times(1)).getUserItems();
    }
}
