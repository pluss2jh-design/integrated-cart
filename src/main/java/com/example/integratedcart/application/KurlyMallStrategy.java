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
public class KurlyMallStrategy implements MallStrategy {

    private final RealMartScraperService realMartScraperService;

    @Override
    public MallType getMallType() {
        return MallType.KURLY;
    }

    @Override
    public List<Product> searchProducts(String keyword, double targetAmount, boolean isLowSugar) {
        log.info("Searching REAL products in KURLY for keyword: {}", keyword);
        return realMartScraperService.scrapeKurly(keyword);
    }
}
