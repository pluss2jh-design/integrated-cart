package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;

import java.util.List;

public interface MallStrategy {
    
    MallType getMallType();

    /**
     * 각 쇼핑몰의 특색에 맞게 최저가 또는 저당 상품을 검색합니다.
     * 실제로는 외부 크롤링/API를 호출할 수 있으나 현재는 DB 기반으로 시뮬레이션합니다.
     * @param keyword 재료명
     * @param targetAmount 필요한 양
     * @param isLowSugar 저당 필터링 여부
     * @return 검색된 상품 리스트
     */
    List<Product> searchProducts(String keyword, double targetAmount, boolean isLowSugar);
}
