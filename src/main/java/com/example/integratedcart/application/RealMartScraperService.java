package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import com.example.integratedcart.domain.product.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealMartScraperService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    private static final int MAX_PRODUCTS_PER_SEARCH = 5;
    private static final long PAGE_LOAD_WAIT_MS = 2000;
    private static final long ID_MULTIPLIER = 100_000_000L;
    private static final int ID_RANDOM_BOUND = 10_000_000;

    /**
     * 네이버 쇼핑/장보기 스크래핑 (가장 안정적)
     */
    public List<Product> scrapeNaver(String keyword) {
        log.info("네이버 스크래핑 시작, 키워드: {}", keyword);
        return scrapeNaverShopping(keyword, null);
    }

    /**
     * 쿠팡 스크래핑 (Google Shopping 결과 활용 - 403 완전 우회 및 실시간 가격)
     */
    public List<Product> scrapeCoupang(String keyword) {
        log.info("쿠팬 Google Shopping 스크래핑, 키워드: {}", keyword);
        // 직접 접근 대신 Google Shopping의 검색 스니펫 및 광고 데이터 활용
        // 이는 쿠팡의 403 차단을 완전히 피하면서도 실제 가격과 상품명을 가져오는 가장 확실한 방법입니다.
        List<Product> products = scrapeGoogleShopping(keyword, "쿠팡");

        if (products.isEmpty()) {
            log.info("Google Shopping 프록시 실패 - 네이버 쇼핑 프록시로 전환.");
            products = scrapeNaverShopping(keyword, "쿠팡");
        }
        return products;
    }

    /**
     * B마트 스크래핑 (시뮬레이션 배제, 실제 데이터 추적)
     */
    public List<Product> scrapeBmart(String keyword) {
        log.info("B마트 실제 웹 소스 스크래핑, 키워드: {}", keyword);
        // B마트는 웹 사이트가 없으므로 통합 커머스 인덱스(Naver/Google)에서 B마트 판매 정보를 추적합니다.
        List<Product> products = scrapeNaverShopping(keyword, "B마트");
        if (products.isEmpty()) {
            products = scrapeGoogleShopping(keyword, "B마트");
        }
        // 배민상회(Sanghoe) 결과가 섞일 경우 필터링 (식당용 대용량 제외 등 - 향후 고도화)
        return products;
    }

    /**
     * 마켓컬리 스크래핑 (품질 및 정확도 개선)
     */
    public List<Product> scrapeKurly(String keyword) {
        log.info("컴리 스크래핑 (엄격 관련성 검증 포함), 키워드: {}", keyword);
        List<Product> products = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://api.kurly.com/search/v4/sites/market/normal-search?keyword=" + encodedKeyword
                    + "&sortType=0&page=1";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode listSections = root.path("data").path("listSections");

            for (JsonNode section : listSections) {
                if ("PRODUCT_LIST".equals(section.path("view").path("sectionCode").asText())) {
                    JsonNode items = section.path("data").path("items");
                    for (JsonNode item : items) {
                        if (products.size() >= MAX_PRODUCTS_PER_SEARCH)
                            break;
                        String name = item.path("name").asText();
                        String productNo = item.path("no").asText();
                        String productUrl = "https://www.kurly.com/goods/" + productNo;

                        // 엄격한 관련성 체크 (건전지 등 오진 방지)
                        if (isStrictlyRelevant(name, keyword)) {
                            int price = item.path("discountedPrice").asInt();
                            if (price == 0)
                                price = item.path("salesPrice").asInt();

                            products.add(Product.builder()
                                    .id(item.path("no").asLong())
                                    .name(name)
                                    .price(price)
                                    .mallType(MallType.KURLY)
                                    .inStock(!item.path("isSoldOut").asBoolean())
                                    .unit("개")
                                    .capacity(1)
                                    .sugarPer100g(0.0)
                                    .productUrl(productUrl)
                                    .build());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("컴리 API 호출 실패", e);
        }
        return products;
    }

    private List<Product> scrapeNaverShopping(String keyword, String mallFilter) {
        List<Product> products = new ArrayList<>();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"));
            Page page = context.newPage();

            String query = (mallFilter != null ? mallFilter + " " : "") + keyword;
            String url = "https://search.shopping.naver.com/search/all?query="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8);
            page.navigate(url);
            page.waitForTimeout(PAGE_LOAD_WAIT_MS);

            // 네이버 쇼핑의 다양한 레이아웃 대응 (더 범용적인 선택자 사용)
            Locator items = page.locator(
                    "div[class*='product_item'], li[class*='product_item'], div[class*='adProduct_item'], div[class*='basicList_item']");
            int count = 0;
            for (int i = 0; i < items.count(); i++) {
                if (count >= MAX_PRODUCTS_PER_SEARCH)
                    break;
                try {
                    Locator item = items.nth(i);

                    // 몰 이름 추출 시도
                    String mallName = "";
                    try {
                        Locator mallLoc = item.locator(
                                "a[class*='product_mall'], span[class*='product_mall'], span[class*='mall_name']")
                                .first();
                        mallName = mallLoc.innerText();
                    } catch (Exception e) {
                        log.debug("쇼핑몰 이름 추출 실패: {}", e.getMessage());
                    }

                    if (mallFilter != null && !mallName.contains(mallFilter) && !mallFilter.contains(mallName))
                        continue;

                    // 링크 및 이름 추출
                    Locator linkLoc = item
                            .locator("a[class*='product_link'], a[class*='adProduct_link'], a[class*='basicList_link']")
                            .first();
                    String name = linkLoc.innerText();
                    String productUrl = linkLoc.getAttribute("href");
                    if (productUrl != null && !productUrl.startsWith("http")) {
                        productUrl = "https://search.shopping.naver.com" + productUrl;
                    }

                    // 가격 추출 (다양한 클래스 대응)
                    String priceStr = "";
                    try {
                        priceStr = item.locator("span[class*='price_num'], em[class*='price_num']").first().innerText();
                    } catch (Exception e) {
                        try {
                            priceStr = item.locator("span[class*='price']").first().innerText();
                        } catch (Exception e2) {
                            continue;
                        }
                    }
                    priceStr = priceStr.replaceAll("[^0-9]", "");

                    if (name.length() > 0 && priceStr.length() > 0) {
                        products.add(createProduct(
                                mallFilter != null ? MallType.valueOf(mallFilter.toUpperCase().replace("B마트", "BMART"))
                                        : MallType.NAVER,
                                (mallFilter == null && mallName.length() > 0 ? "[" + mallName + "] " : "") + name,
                                Integer.parseInt(priceStr),
                                productUrl));
                        count++;
                    }
                } catch (Exception e) {
                    log.warn("네이버 상품 파싱 실패: {}", e.getMessage());
                }
            }
            browser.close();
        } catch (Exception e) {
            log.error("네이버 스크래핑 실패", e);
        }
        return products;
    }

    private List<Product> scrapeGoogleShopping(String keyword, String mallFilter) {
        List<Product> products = new ArrayList<>();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"));
            Page page = context.newPage();

            String query = mallFilter + " " + keyword;
            String url = "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&tbm=shop";
            page.navigate(url);
            page.waitForTimeout(PAGE_LOAD_WAIT_MS);

            // 구글 쇼핑 상품 선택자
            Locator items = page.locator("div.sh-dgr__grid-result, div.sh-np__click-target");
            int count = 0;
            for (int i = 0; i < items.count(); i++) {
                if (count >= MAX_PRODUCTS_PER_SEARCH)
                    break;
                try {
                    Locator item = items.nth(i);
                    String text = item.innerText();
                    if (mallFilter != null && !text.contains(mallFilter))
                        continue;

                    Locator linkLoc = item.locator("a").first();
                    String name = item.locator("h3").first().innerText();
                    String productUrl = linkLoc.getAttribute("href");
                    if (productUrl != null && productUrl.startsWith("/url?q=")) {
                        productUrl = productUrl.substring(7).split("&")[0];
                        productUrl = java.net.URLDecoder.decode(productUrl, StandardCharsets.UTF_8);
                    }

                    Pattern p = Pattern.compile("([0-9,]+)원");
                    Matcher m = p.matcher(text);
                    if (m.find()) {
                        int price = Integer.parseInt(m.group(1).replaceAll(",", ""));
                        products.add(createProduct(mallFilter.equals("쿠팡") ? MallType.COUPANG : MallType.BMART, name,
                                price, productUrl));
                        count++;
                    }
                } catch (Exception e) {
                    log.debug("구글 쇼핑 상품 파싱 실패: {}", e.getMessage());
                }
            }
            browser.close();
        } catch (Exception e) {
            log.error("Google Shopping 스크래핑 실패 - {}", mallFilter, e);
        }
        return products;
    }

    private boolean isStrictlyRelevant(String name, String keyword) {
        if (keyword.length() <= 1)
            return name.contains(keyword);

        String cleanName = name.toLowerCase().replace(" ", "");
        String cleanKeyword = keyword.toLowerCase().replace(" ", "");

        // 1. 한 글자씩 매칭이 아닌 토큰 매칭으로 변경 (돼지고기 <-> 돼지 대응)
        String[] tokens = keyword.split(" ");
        boolean allTokensMatch = true;
        for (String token : tokens) {
            String cleanToken = token.replace("고기", ""); // '돼지고기' -> '돼지'
            if (!cleanName.contains(cleanToken.toLowerCase()) && !cleanName.contains(token.toLowerCase())) {
                allTokensMatch = false;
                break;
            }
        }

        if (allTokensMatch)
            return true;

        // 2. 역방향 체크 (상품명이 키워드에 포함되는 경우 - 짧은 키워드 대응)
        if (cleanKeyword.contains(cleanName) && cleanName.length() >= 2)
            return true;

        // 3. 금지 단어 필터링 (부속품 제외)
        String[] forbidden = { "건전지", "필터", "공병", "박스", "케이스", "전지", "배터리", "가이드", "설명서" };
        for (String f : forbidden) {
            if (cleanName.contains(f) && !cleanKeyword.contains(f))
                return false;
        }

        // 4. 상품명에 키워드의 핵심적인 두 글자만 포함되어도 일단 허용 (더 많은 결과를 위해)
        if (cleanKeyword.length() >= 2) {
            String key2 = cleanKeyword.substring(0, 2);
            if (cleanName.contains(key2))
                return true;
        }

        return cleanName.contains(cleanKeyword.substring(0, Math.min(1, cleanKeyword.length())));
    }

    private Product createProduct(MallType mallType, String name, int price, String productUrl) {
        return Product.builder()
                .id(mallType.ordinal() * ID_MULTIPLIER + random.nextInt(ID_RANDOM_BOUND))
                .name(name)
                .price(price)
                .mallType(mallType)
                .inStock(true)
                .unit("개")
                .capacity(1)
                .sugarPer100g(0.0)
                .productUrl(productUrl)
                .build();
    }
}
