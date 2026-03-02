package com.example.integratedcart.application;

import com.example.integratedcart.domain.recipe.Recipe;
import com.example.integratedcart.domain.recipe.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    @Value("${google.ai.api-key}")
    private String apiKey;

    /**
     * 신형 Google Gen AI SDK (google-genai)를 사용하여 레시피를 분석합니다.
     */
    public Recipe extractAndSaveRecipe(String input, String modelName) {
        String cleanedInput = input.trim();
        log.info("레시피 분석 시작 (모델: {}): {}", modelName, cleanedInput);

        if (!cleanedInput.startsWith("http")) {
            Optional<Recipe> cached = recipeRepository.findAll().stream()
                    .filter(r -> r.getName().equals(cleanedInput))
                    .findFirst();
            if (cached.isPresent()) {
                log.info("캐시된 레시피 반환: {}", cleanedInput);
                return cached.get();
            }
        }

        String contentToAnalyze = cleanedInput;
        if (cleanedInput.startsWith("http")) {
            contentToAnalyze = fetchPageContent(cleanedInput);
        }

        try {
            // 신형 SDK 사용 패턴 (Client 클래스 활용)
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();
            
            String prompt = String.format(
                "당신은 전문 요리사입니다. 다음 입력값(음식명 또는 페이지 내용)을 분석하여 필요한 재료 리스트를 JSON 형식으로 응답하세요.\n" +
                "응답 형식: { \"name\": \"음식명\", \"ingredients\": [ { \"name\": \"재료명\", \"amount\": 수량(숫자), \"unit\": \"단위\" } ], \"basePortion\": 기준인분(숫자) }\n" +
                "입력값: %s", contentToAnalyze
            );

            GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
            String jsonResponse = response.text();
            
            if (jsonResponse.contains("```json")) {
                jsonResponse = jsonResponse.substring(jsonResponse.indexOf("```json") + 7, jsonResponse.lastIndexOf("```"));
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            String recipeName = root.path("name").asText(cleanedInput.startsWith("http") ? "분석된 요리" : cleanedInput);
            String ingredientsJson = objectMapper.writeValueAsString(root.path("ingredients"));
            int basePortion = root.path("basePortion").asInt(1);

            Recipe recipe = Recipe.builder()
                    .name(recipeName)
                    .ingredientsJson(ingredientsJson)
                    .basePortion(basePortion)
                    .build();

            return recipeRepository.save(recipe);

        } catch (Exception e) {
            log.error("신형 SDK AI 분석 실패", e);
            throw new RuntimeException("모델 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String fetchPageContent(String url) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(url);
            String content = page.title() + " " + page.innerText("body").substring(0, Math.min(2000, page.innerText("body").length()));
            browser.close();
            return content;
        } catch (Exception e) {
            log.warn("페이지 텍스트 추출 실패: {}", url);
            return url;
        }
    }
}
