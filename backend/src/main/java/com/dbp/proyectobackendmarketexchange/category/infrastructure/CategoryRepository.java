package com.dbp.proyectobackendmarketexchange.category.infrastructure;


import com.dbp.proyectobackendmarketexchange.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
