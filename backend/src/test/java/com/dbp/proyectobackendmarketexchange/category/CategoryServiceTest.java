package com.dbp.proyectobackendmarketexchange.category;

import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import com.dbp.proyectobackendmarketexchange.category.domain.CategoryService;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryRequestDto;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryResponseDto;
import com.dbp.proyectobackendmarketexchange.category.infrastructure.CategoryRepository;
import com.dbp.proyectobackendmarketexchange.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllCategories() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Books");

        List<Category> categories = List.of(category1, category2);

        CategoryResponseDto responseDto1 = new CategoryResponseDto();
        responseDto1.setId(1L);
        responseDto1.setName("Electronics");

        CategoryResponseDto responseDto2 = new CategoryResponseDto();
        responseDto2.setId(2L);
        responseDto2.setName("Books");

        when(categoryRepository.findAll()).thenReturn(categories);
        when(modelMapper.map(category1, CategoryResponseDto.class)).thenReturn(responseDto1);
        when(modelMapper.map(category2, CategoryResponseDto.class)).thenReturn(responseDto2);

        List<CategoryResponseDto> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Books", result.get(1).getName());

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    public void testGetCategoryById_Success() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setItems(List.of());  // Inicializa la lista vacía

        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryResponseDto.class)).thenReturn(responseDto);

        CategoryResponseDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(1L));

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateCategory() {
        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setName("Home Appliances");

        Category category = new Category();
        category.setId(1L);
        category.setName("Home Appliances");

        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Home Appliances");

        when(modelMapper.map(requestDto, Category.class)).thenReturn(category);
        when(modelMapper.map(category, CategoryResponseDto.class)).thenReturn(responseDto);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponseDto result = categoryService.createCategory(requestDto);

        assertNotNull(result);
        assertEquals("Home Appliances", result.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testUpdateCategory_Success() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setName("Updated Electronics");

        CategoryResponseDto responseDto = new CategoryResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Electronics");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(any(CategoryRequestDto.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryResponseDto.class)).thenReturn(responseDto);

        CategoryResponseDto result = categoryService.updateCategory(1L, requestDto);

        assertNotNull(result);
        assertEquals("Updated Electronics", result.getName());

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    public void testUpdateCategory_NotFound() {
        CategoryRequestDto requestDto = new CategoryRequestDto();
        requestDto.setName("Non-existent");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(1L, requestDto));

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteCategory_Success() {
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }
}