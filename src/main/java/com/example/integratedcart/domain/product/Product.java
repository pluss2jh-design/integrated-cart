package com.example.integratedcart.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MallType mallType;

    // 100g 당 당류 (저당 필터링용)
    @Column(name = "sugar_per_100g")
    private Double sugarPer100g;

    @Column(nullable = false)
    private String unit; // e.g., "g", "ml", "개"

    @Column(nullable = false)
    private Integer capacity; // 용량 숫자 (예: unit이 g이고 capacity가 500이면 500g)

    @Column(nullable = false)
    private Boolean inStock;

    @Builder
    public Product(Long id, String name, Integer price, MallType mallType, Double sugarPer100g, String unit, Integer capacity, Boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.mallType = mallType;
        this.sugarPer100g = sugarPer100g;
        this.unit = unit;
        this.capacity = capacity;
        this.inStock = inStock != null ? inStock : true;
    }

    public void updateStock(Boolean inStock) {
        this.inStock = inStock;
    }
}
