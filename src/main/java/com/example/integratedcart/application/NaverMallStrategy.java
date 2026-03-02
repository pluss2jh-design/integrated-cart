package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverMallStrategy implements MallStrategy {

    private final RealMartScraperService realMartScraperService;

    @Override
    public MallType getMallType() {
        return MallType.NAVER;
    }

    @Override
    public List<Product> searchProducts(String keyword, double targetAmount, boolean isLowSugar) {
        log.info("네이버 상품 검색 중, 키워드: {}", keyword);
        return realMartScraperService.scrapeNaver(keyword);
    }
}
