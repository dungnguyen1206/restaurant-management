package com.rroms.restaurantmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String itemName;

    private BigDecimal price;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String description;

    private String imageUrl;

    private Boolean isSoldOut;

    private Integer virtualInStock;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "menuItem")
    private List<OrderItem> orderItems = new ArrayList<>();
}
