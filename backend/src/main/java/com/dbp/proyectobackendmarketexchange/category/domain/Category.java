package com.dbp.proyectobackendmarketexchange.category.domain;

import com.dbp.proyectobackendmarketexchange.item.domain.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;


    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Item> items;
}