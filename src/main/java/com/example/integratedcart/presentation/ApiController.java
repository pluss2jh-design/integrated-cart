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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PostMapping("/analyze")
    public ResponseEntity<Recipe> analyze(@RequestBody ApiDto.AnalyzeRequest request) {
        log.info("Analyze request: {}", request.getInput());
        Recipe recipe = recipeService.extractAndSaveRecipe(request.getInput());
        return ResponseEntity.ok(recipe);
    }

    @GetMapping("/ingredients/search")
    public ResponseEntity<Map<MallType, List<Product>>> searchIngredients(
            @RequestParam String keyword,
            @RequestParam double requiredAmount,
            @RequestParam boolean lowSugar) {
        
        log.info("Search request -> keyword: {}, amount: {}, lowSugar: {}", keyword, requiredAmount, lowSugar);
        Map<MallType, List<Product>> results = new HashMap<>();

        for (MallType type : MallType.values()) {
            MallStrategy strategy = mallStrategyFactory.getStrategy(type);
            List<Product> products = strategy.searchProducts(keyword, requiredAmount, lowSugar);
            results.put(type, products);
        }

        return ResponseEntity.ok(results);
    }

    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(@RequestBody ApiDto.CartAddRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });

        CartItem item = CartItem.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();

        cart.addItem(item);
        cartRepository.save(cart);

        return ResponseEntity.ok("Item added to cart successfully");
    }

    @PostMapping("/order/checkout")
    public ResponseEntity<String> checkout(@RequestBody ApiDto.CheckoutRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 사용자가 선택한 쇼핑몰에 대해 각각 플레이라이트 결제 스크립트 실행 시뮬레이션
        for (String mallTypeStr : request.getMallTypes()) {
            MallType type = MallType.valueOf(mallTypeStr.toUpperCase());
            String encryptedCreds = getCredentialsFromUser(user, type);
            
            String decCreds = "";
            if (encryptedCreds != null) {
                try {
                    decCreds = aes256Util.decrypt(encryptedCreds);
                } catch (Exception e) {
                    log.error("Decryption failed for mall {}", type, e);
                }
            }
            
            // 실제 구현 시 장바구니에 담긴 해당 몰의 상품 리스트를 JSON 화하여 넘깁니다.
            boolean success = scraperService.executeAutoCheckout(type.name(), decCreds, "[{...}]");
            log.info("Checkout for {} result: {}", type, success);
        }

        return ResponseEntity.ok("Checkout process initiated successfully");
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
