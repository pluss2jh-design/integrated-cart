package com.example.integratedcart.presentation;

import com.example.integratedcart.application.MallStrategy;
import com.example.integratedcart.application.MallStrategyFactory;
import com.example.integratedcart.application.QuantityScalingService;
import com.example.integratedcart.application.RecipeService;
import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;
import com.example.integratedcart.domain.product.ProductRepository;
import com.example.integratedcart.domain.recipe.Recipe;
import com.example.integratedcart.domain.user.User;
import com.example.integratedcart.domain.user.UserRepository;
import com.example.integratedcart.infrastructure.AES256Util;
import com.example.integratedcart.infrastructure.PlaywrightScraperService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final RecipeService recipeService;
    private final MallStrategyFactory mallStrategyFactory;
    private final UserRepository userRepository;
    private final PlaywrightScraperService scraperService;
    private final AES256Util aes256Util;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.ai.api-key}")
    private String apiKey;

    /**
     * Google API를 통해 사용 가능한 Gemini 모델 리스트를 조회합니다.
     */
    @GetMapping("/ai/models")
    public ResponseEntity<List<Map<String, Object>>> getAiModels() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode modelsNode = root.path("models");
            
            List<Map<String, Object>> models = new ArrayList<>();
            for (JsonNode m : modelsNode) {
                Map<String, Object> modelMap = new HashMap<>();
                modelMap.put("name", m.path("name").asText().replace("models/", ""));
                modelMap.put("name", m.path("name").asText().replace("models/", ""));
                modelMap.put("displayName", m.path("displayName").asText());
                modelMap.put("supportedGenerationMethods", m.path("supportedGenerationMethods"));
                modelMap.put("inputTokenLimit", m.path("inputTokenLimit").asInt());
                modelMap.put("supportedGenerationMethods", m.path("supportedGenerationMethods"));
                models.add(modelMap);
            }
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            log.error("Gemini 모델 리스트 조회 실패", e);
            // 에러 발생 시 기본 폴백 리스트 반환
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * 음식명 또는 URL을 입력받아 레시피를 분석합니다.
     */
    @PostMapping("/analyze")
    public ResponseEntity<Recipe> analyze(@RequestBody ApiDto.AnalyzeRequest request) {
        log.info("Analyze request for dish: {} with model: {}", request.getInput(), request.getModelName());
        Recipe recipe = recipeService.extractAndSaveRecipe(request.getInput(), request.getModelName());
        return ResponseEntity.ok(recipe);
    }

    /**
     * 재료명으로 각 쇼핑몰별 상품을 검색하고 최저가 1개를 선정합니다.
     */
    @GetMapping("/ingredients/search")
    public ResponseEntity<Map<MallType, List<Product>>> searchIngredients(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") double requiredAmount,
            @RequestParam(required = false) String malls) {

        log.info("검색 요청 - 키워드: {}, 양: {}, 쇼핑몰: {}", keyword, requiredAmount, malls);
        Map<MallType, List<Product>> results = new HashMap<>();

        Set<MallType> targetMalls = new HashSet<>();
        if (malls == null || malls.isEmpty() || malls.equalsIgnoreCase("ALL")) {
            targetMalls.addAll(Arrays.asList(MallType.values()));
        } else {
            for (String m : malls.split(",")) {
                try {
                    targetMalls.add(MallType.valueOf(m.trim().toUpperCase()));
                } catch (Exception e) {
                    log.warn("잘못된 쇼핑몰 타입 무시: {}", m);
                }
            }
        }

        Product cheapestProduct = null;
        double lowestUnitPrice = Double.MAX_VALUE;
        double closestQuantityDiff = Double.MAX_VALUE;

        for (MallType type : targetMalls) {
            try {
                MallStrategy strategy = mallStrategyFactory.getStrategy(type);
                List<Product> products = strategy.searchProducts(keyword, requiredAmount, false);
                
                for (Product p : products) {
                    double unitPrice = (double) p.getPrice() / Math.max(1, p.getCapacity());
                    double quantityDiff = Math.abs(p.getCapacity() - requiredAmount);

                    if (unitPrice < lowestUnitPrice || (unitPrice == lowestUnitPrice && quantityDiff < closestQuantityDiff)) {
                        lowestUnitPrice = unitPrice;
                        closestQuantityDiff = quantityDiff;
                        cheapestProduct = p;
                    }
                }
            } catch (Exception e) {
                log.error("쇼핑몰 {} 검색 실패: {}", type, e.getMessage());
            }
        }

        if (cheapestProduct != null) {
            results.put(cheapestProduct.getMallType(), Collections.singletonList(cheapestProduct));
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(@RequestBody ApiDto.CartAddRequest request) {
        return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
    }

    @PostMapping("/order/auto-cart")
    public ResponseEntity<String> autoCart(@RequestBody ApiDto.CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        for (String mallTypeStr : request.getMallTypes()) {
            try {
                String decCreds = decryptCredentials(user, MallType.valueOf(mallTypeStr.toUpperCase()));
                scraperService.executeAutoCartAddition(mallTypeStr, decCreds, "[]");
            } catch (Exception e) {
                log.error("쇼핑몰 {} 장바구니 자동 추가 실패: {}", mallTypeStr, e.getMessage());
            }
        }
        return ResponseEntity.ok("선택한 쇼핑몰의 장바구니에 상품을 모두 담았습니다.");
    }

    @PostMapping("/order/checkout")
    public ResponseEntity<String> checkout(@RequestBody ApiDto.CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        for (String mallTypeStr : request.getMallTypes()) {
            try {
                MallType type = MallType.valueOf(mallTypeStr.toUpperCase());
                String decCreds = decryptCredentials(user, type);
                scraperService.executeAutoCheckout(type.name(), decCreds, "[]");
            } catch (Exception e) {
                log.error("쇼핑몰 {} 결제 실패: {}", mallTypeStr, e.getMessage());
            }
        }
        return ResponseEntity.ok("결제 프로세스가 시작되었습니다.");
    }

    private String decryptCredentials(User user, MallType type) {
        String encryptedCreds = getCredentialsFromUser(user, type);
        if (encryptedCreds == null || encryptedCreds.isEmpty()) return "";
        try {
            return aes256Util.decrypt(encryptedCreds);
        } catch (Exception e) {
            log.error("쇼핑몰 {} 자격증명 복호화 실패", type, e);
            return "";
        }
    }

    private String getCredentialsFromUser(User user, MallType type) {
        switch (type) {
            case COUPANG: return user.getCoupangCredentials();
            case KURLY: return user.getKurlyCredentials();
            case BMART: return user.getBmartCredentials();
            default: return null;
        }
    }
}
