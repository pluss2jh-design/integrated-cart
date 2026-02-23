package com.example.integratedcart.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByMallTypeAndInStockTrue(MallType mallType);

    // 이름 기반 검색 (최저가 순)
    List<Product> findByNameContainingAndInStockTrueOrderByPriceAsc(String keyword);

    // 이름 기반 검색 (저당 순, 100g당 당류가 낮은 순서)
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.inStock = true ORDER BY p.sugarPer100g ASC NULLS LAST")
    List<Product> findByNameContainingOrderBySugarAsc(@Param("keyword") String keyword);
}
