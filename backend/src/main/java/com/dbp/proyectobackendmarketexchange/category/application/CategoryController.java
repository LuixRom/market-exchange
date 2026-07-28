package com.dbp.proyectobackendmarketexchange.category.application;

import com.dbp.proyectobackendmarketexchange.category.domain.CategoryService;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryRequestDto;
import com.dbp.proyectobackendmarketexchange.category.dto.CategoryResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto createCategoryDTO) {
        CategoryResponseDto category = categoryService.createCategory(createCategoryDTO);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto updateCategory) {
        CategoryResponseDto category = categoryService.updateCategory(id, updateCategory);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
