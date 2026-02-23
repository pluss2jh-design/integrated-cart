package com.example.integratedcart.presentation;

import lombok.Data;
import java.util.List;

public class ApiDto {

    @Data
    public static class AnalyzeRequest {
        private String input; // 영상 URL 또는 음식명
    }

    @Data
    public static class SearchRequest {
        private String keyword;
        private double requiredAmount;
        private boolean lowSugar;
    }

    @Data
    public static class CartAddRequest {
        private Long userId;
        private Long productId;
        private Integer quantity;
    }

    @Data
    public static class CheckoutRequest {
        private Long userId;
        private List<String> mallTypes; // e.g., ["COUPANG", "KURLY"]
    }
}
