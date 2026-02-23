package com.example.integratedcart.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuantityScalingService {

    /**
     * 레시피의 기준 인분수와 사용자가 원하는 인분수에 따라 필요한 식재료 양을 계산합니다.
     * @param originalAmount 레시피에 적힌 원재료 양
     * @param basePortion 레시피 기준 인분 수
     * @param targetPortion 사용자가 원하는 인분 수
     * @return 계산된 목표 양
     */
    public double calculateTargetAmount(double originalAmount, int basePortion, int targetPortion) {
        if (basePortion <= 0 || targetPortion <= 0) {
            throw new IllegalArgumentException("Portion must be greater than 0");
        }
        
        double requiredAmount = (originalAmount / basePortion) * targetPortion;
        log.info("Scaled amount: {} -> {} (base: {}, target: {})", originalAmount, requiredAmount, basePortion, targetPortion);
        return requiredAmount;
    }

    /**
     * 쇼핑몰에서 판매하는 상품의 단위 용량에 맞춰 구매해야 할 최소 수량을 계산합니다.
     * @param requiredAmount 필요한 총량
     * @param productCapacity 판매 상품의 1개당 용량
     * @return 구매 수량 (개수)
     */
    public int calculatePurchaseQuantity(double requiredAmount, int productCapacity) {
        if (productCapacity <= 0) {
            throw new IllegalArgumentException("Product capacity must be greater than 0");
        }
        
        int quantity = (int) Math.ceil(requiredAmount / productCapacity);
        log.info("Required: {}, Capacity: {}, Purchase Quantity: {}", requiredAmount, productCapacity, quantity);
        return quantity;
    }
}
