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

    /**
     * 영상 URL이나 음식명을 입력받아 레시피 정보를 추출하고 저장합니다.
     * @param input URL 또는 음식명
     * @return 저장된 Recipe 엔티티
     */
    public Recipe extractAndSaveRecipe(String input) {
        log.info("Extracting recipe for input: {}", input);
        
        String ingredientsJson;
        String recipeName = input.startsWith("http") ? "추출된 레시피" : input;
        Integer basePortion = 1;

        // 단순 식재료 입력인지 확인 (공백이 없고 URL이 아니며 짧은 경우)
        if (!input.startsWith("http") && !input.contains(" ") && input.length() < 10) {
            log.info("Single ingredient detected: {}", input);
            ingredientsJson = String.format("[{\"name\":\"%s\", \"amount\": 1, \"unit\":\"개\"}]", input);
        } else {
            // 레시피명이나 URL인 경우 (Mock Data)
            ingredientsJson = "[{\"name\":\"양파\", \"amount\": 100, \"unit\":\"g\"}, {\"name\":\"소고기\", \"amount\": 200, \"unit\":\"g\"}]";
            basePortion = 2;
        }

        Recipe recipe = Recipe.builder()
                .name(recipeName)
                .ingredientsJson(ingredientsJson)
                .basePortion(basePortion)
                .build();

        return recipeRepository.save(recipe);
    }
}
