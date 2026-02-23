package com.example.integratedcart.application;

import com.example.integratedcart.domain.recipe.Recipe;
import com.example.integratedcart.domain.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    // 향후 Gemini API 설정을 주입받을 클라이언트 (현재는 Mock 처리)
    // private final GeminiClient geminiClient;

    /**
     * 영상 URL이나 음식명을 입력받아 Gemini API를 통해 레시피 정보를 추출하고 저장합니다.
     * @param input URL 또는 음식명
     * @return 저장된 Recipe 엔티티
     */
    public Recipe extractAndSaveRecipe(String input) {
        log.info("Extracting recipe for input: {}", input);
        
        // TODO: 실제 Gemini API 연동 (Mock Data로 대체)
        String mockIngredientsJson = "[{\"name\":\"양파\", \"amount\": 100, \"unit\":\"g\"}, {\"name\":\"소고기\", \"amount\": 200, \"unit\":\"g\"}]";
        Integer mockBasePortion = 2; // 기준 인분 (2인분)
        String mockRecipeName = input.startsWith("http") ? "추출된 레시피" : input;

        Recipe recipe = Recipe.builder()
                .name(mockRecipeName)
                .ingredientsJson(mockIngredientsJson)
                .basePortion(mockBasePortion)
                .build();

        return recipeRepository.save(recipe);
    }
}
