package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;
import com.example.integratedcart.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoupangMallStrategy implements MallStrategy {

    private final ProductRepository productRepository;

    @Override
    public MallType getMallType() {
        return MallType.COUPANG;
    }

    @Override
    public List<Product> searchProducts(String keyword, double targetAmount, boolean isLowSugar) {
        log.info("Searching products in COUPANG for keyword: {}, lowSugar: {}", keyword, isLowSugar);
        
        List<Product> products;
        if (isLowSugar) {
            products = productRepository.findByNameContainingOrderBySugarAsc(keyword);
        } else {
            products = productRepository.findByNameContainingAndInStockTrueOrderByPriceAsc(keyword);
        }

        // 쿠팡몰 상품만 필터링
        return products.stream()
                .filter(p -> p.getMallType() == MallType.COUPANG)
                .collect(Collectors.toList());
    }
}
