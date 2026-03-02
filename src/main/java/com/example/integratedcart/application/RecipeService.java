package com.example.integratedcart.application;

import com.example.integratedcart.domain.recipe.Recipe;
import com.example.integratedcart.domain.recipe.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
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
     * Gemini AI를 사용하여 음식명 또는 URL로부터 레시피를 분석합니다.
     */
    public Recipe extractAndSaveRecipe(String input, String modelName) {
        String cleanedInput = input.trim();
        log.info("레시피 분석 시작 (모델: {}): {}", modelName, cleanedInput);

        // 1. DB 캐시 확인 (음식명인 경우)
        if (!cleanedInput.startsWith("http")) {
            Optional<Recipe> cached = recipeRepository.findAll().stream()
                    .filter(r -> r.getName().equals(cleanedInput))
                    .findFirst();
            if (cached.isPresent()) {
                log.info("캐시된 레시피 반환: {}", cleanedInput);
                return cached.get();
            }
        }

        // 2. 동영상 링크인 경우 텍스트 추출 시도 (간이 구현 - 메타데이터 등)
        String contentToAnalyze = cleanedInput;
        if (cleanedInput.startsWith("http")) {
            contentToAnalyze = fetchPageContent(cleanedInput);
        }

        // 3. Gemini AI 호출
        try (VertexAI vertexAI = new VertexAI("integrated-cart", "asia-northeast3")) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            
            String prompt = String.format(
                "당신은 전문 요리사입니다. 다음 입력값(음식명 또는 페이지 내용)을 분석하여 필요한 재료 리스트를 JSON 형식으로 응답하세요.\n" +
                "응답 형식: { \"name\": \"음식명\", \"ingredients\": [ { \"name\": \"재료명\", \"amount\": 수량(숫자), \"unit\": \"단위\" } ], \"basePortion\": 기준인분(숫자) }\n" +
                "입력값: %s", contentToAnalyze
            );

            var response = model.generateContent(prompt);
            String jsonResponse = ResponseHandler.getText(response);
            
            // JSON 부분만 추출 (마크다운 가드 제거 등)
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
            log.error("AI 분석 실패", e);
            throw new RuntimeException("유효하지 않은 AI 모델이거나 분석 중 오류가 발생했습니다. 다른 모델을 선택해주세요.", e);
        }
    }

    private String fetchPageContent(String url) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(url);
            // 간단하게 제목과 본문 일부만 가져옴
            String content = page.title() + " " + page.innerText("body").substring(0, Math.min(2000, page.innerText("body").length()));
            browser.close();
            return content;
        } catch (Exception e) {
            log.warn("페이지 텍스트 추출 실패, URL 그대로 사용: {}", url);
            return url;
        }
    }
}
