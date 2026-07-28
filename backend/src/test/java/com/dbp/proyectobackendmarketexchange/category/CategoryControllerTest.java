package com.dbp.proyectobackendmarketexchange.category;

import com.dbp.proyectobackendmarketexchange.category.application.CategoryController;
import com.dbp.proyectobackendmarketexchange.category.domain.CategoryService;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryRequestDto;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    public void testCreateCategory() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setName("Electronics");

        CategoryResponseDto categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(1L);
        categoryResponseDto.setName("Electronics");

        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(categoryResponseDto);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(categoryRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService, times(1)).createCategory(any(CategoryRequestDto.class));
    }

    @Test
    public void testGetCategoryById() throws Exception {
        CategoryResponseDto categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(1L);
        categoryResponseDto.setName("Electronics");

        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponseDto);

        mockMvc.perform(get("/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    public void testGetAllCategories() throws Exception {
        CategoryResponseDto category1 = new CategoryResponseDto();
        category1.setId(1L);
        category1.setName("Electronics");

        CategoryResponseDto category2 = new CategoryResponseDto();
        category2.setId(2L);
        category2.setName("Books");

        List<CategoryResponseDto> categories = List.of(category1, category2);

        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Books"));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    public void testUpdateCategory() throws Exception {
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setName("Home Appliances");

        CategoryResponseDto updatedCategory = new CategoryResponseDto();
        updatedCategory.setId(1L);
        updatedCategory.setName("Home Appliances");

        when(categoryService.updateCategory(eq(1L), any(CategoryRequestDto.class))).thenReturn(updatedCategory);

        mockMvc.perform(put("/category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(categoryRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Home Appliances"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(CategoryRequestDto.class));
    }

    @Test
    public void testDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/category/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }


    public static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
