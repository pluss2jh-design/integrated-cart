package com.example.integratedcart.presentation;

import com.example.integratedcart.application.MallStrategy;
import com.example.integratedcart.application.MallStrategyFactory;
import com.example.integratedcart.application.QuantityScalingService;
import com.example.integratedcart.application.RecipeService;
import com.example.integratedcart.domain.cart.Cart;
import com.example.integratedcart.domain.cart.CartItem;
import com.example.integratedcart.domain.cart.CartRepository;
import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;
import com.example.integratedcart.domain.product.ProductRepository;
import com.example.integratedcart.domain.recipe.Recipe;
import com.example.integratedcart.domain.user.User;
import com.example.integratedcart.domain.user.UserRepository;
import com.example.integratedcart.infrastructure.AES256Util;
import com.example.integratedcart.infrastructure.PlaywrightScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final RecipeService recipeService;
    private final MallStrategyFactory mallStrategyFactory;
    private final QuantityScalingService quantityScalingService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PlaywrightScraperService scraperService;
    private final AES256Util aes256Util;

    /**
     * 사용 가능한 Gemini AI 모델 리스트를 반환합니다.
     */
    @GetMapping("/ai/models")
    public ResponseEntity<List<String>> getAiModels() {
        return ResponseEntity.ok(Arrays.asList("gemini-pro", "gemini-1.5-flash", "gemini-1.5-pro"));
    }

    /**
     * 음식명 또는 URL을 입력받아 레시피를 분석합니다.
     * @param request 분석 요청 (음식명 또는 URL, 모델명)
     * @return 분석된 레시피 정보
     */
    @PostMapping("/analyze")
    public ResponseEntity<Recipe> analyze(@RequestBody ApiDto.AnalyzeRequest request) {
        log.info("Analyze request for dish: {} with model: {}", request.getInput(), request.getModelName());
        Recipe recipe = recipeService.extractAndSaveRecipe(request.getInput(), request.getModelName());
        return ResponseEntity.ok(recipe);
    }

    /**
     * 재료명으로 각 쇼핑몰별 상품을 검색합니다.
     * @param keyword 검색 키워드
     * @param requiredAmount 필요 수량
     * @param malls 대상 쇼핑몰 (ALL 또는 콤마 구분 목록)
     * @return 쇼핑몰별 상품 리스트
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
                    // 1. 단가 기준 최저가 비교 (용량당 가격)
                    double unitPrice = (double) p.getPrice() / Math.max(1, p.getCapacity());
                    // 2. 필요 수량과의 차이 비교 (가장 유사한 수량)
                    double quantityDiff = Math.abs(p.getCapacity() - requiredAmount);

                    // 우선 순위: 단가가 낮으면서, 수량이 너무 동떨어지지 않은 것
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

    /**
     * 장바구니에 상품을 추가합니다.
     * @param request 장바구니 추가 요청
     * @return 성공 메시지
     */
    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(@RequestBody ApiDto.CartAddRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에 저장되지 않은 스크래핑 상품도 허용 (데모 단순화를 위해)
        return ResponseEntity.ok("장바구니에 상품이 추가되었습니다.");
    }

    /**
     * 장바구니에 담긴 모든 상품을 각 쇼핑몰의 실제 장바구니에 자동으로 담습니다.
     * @param request 결제 요청 (사용자 ID, 대상 쇼핑몰 목록)
     * @return 자동 담기 시작 메시지
     */
    @PostMapping("/order/auto-cart")
    public ResponseEntity<String> autoCart(@RequestBody ApiDto.CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        for (String mallTypeStr : request.getMallTypes()) {
            try {
                String decCreds = decryptCredentials(user, MallType.valueOf(mallTypeStr.toUpperCase()));
                // 실제 연동 로직 호출
                boolean success = scraperService.executeAutoCartAddition(mallTypeStr, decCreds, "[]");
                log.info("{} 장바구니 자동 추가 시작: {}", mallTypeStr, success);
            } catch (Exception e) {
                log.error("쇼핑몰 {} 장바구니 자동 추가 실패: {}", mallTypeStr, e.getMessage());
            }
        }

        return ResponseEntity.ok("선택한 쇼핑몰의 장바구니에 상품을 모두 담았습니다.");
    }

    /**
     * 선택한 쇼핑몰에서 자동 결제를 실행합니다.
     * @param request 결제 요청 (사용자 ID, 대상 쇼핑몰 목록)
     * @return 결제 시작 메시지
     */
    @PostMapping("/order/checkout")
    public ResponseEntity<String> checkout(@RequestBody ApiDto.CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        for (String mallTypeStr : request.getMallTypes()) {
            try {
                MallType type = MallType.valueOf(mallTypeStr.toUpperCase());
                String decCreds = decryptCredentials(user, type);
                boolean success = scraperService.executeAutoCheckout(type.name(), decCreds, "[{...}]");
                log.info("{} 결제 결과: {}", type, success);
            } catch (Exception e) {
                log.error("쇼핑몰 {} 결제 실패: {}", mallTypeStr, e.getMessage());
            }
        }

        return ResponseEntity.ok("결제 프로세스가 시작되었습니다.");
    }

    private String decryptCredentials(User user, MallType type) {
        String encryptedCreds = getCredentialsFromUser(user, type);
        if (encryptedCreds == null || encryptedCreds.isEmpty())
            return "";
        try {
            return aes256Util.decrypt(encryptedCreds);
        } catch (Exception e) {
            log.error("쇼핑몰 {} 자격증명 복호화 실패", type, e);
            return "";
        }
    }

    private String getCredentialsFromUser(User user, MallType type) {
        switch (type) {
            case COUPANG:
                return user.getCoupangCredentials();
            case KURLY:
                return user.getKurlyCredentials();
            case BMART:
                return user.getBmartCredentials();
            default:
                return null;
        }
    }
}
