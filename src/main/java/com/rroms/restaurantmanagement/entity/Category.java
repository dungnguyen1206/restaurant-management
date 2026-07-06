package com.rroms.restaurantmanagement.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, columnDefinition = "NVARCHAR(500)")
    private String categoryName;

    private Instant createAt;

    private Instant updateAt;

    @OneToMany(mappedBy = "category")
    private List<MenuItem> menuItems = new ArrayList<>();
}