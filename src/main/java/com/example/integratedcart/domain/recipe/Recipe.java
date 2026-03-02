package com.example.integratedcart.domain.recipe;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // JSON 형식으로 저장하며, String 또는 별도 JSON 타입을 사용할 수 있음
    @Column(name = "ingredients_json", columnDefinition = "TEXT", nullable = false)
    private String ingredientsJson;

    // 기본 인분 수 (예: 레시피에 표시된 기준 인분수)
    @Column(name = "base_portion", nullable = false)
    private Integer basePortion;

    @Builder
    public Recipe(String name, String ingredientsJson, Integer basePortion) {
        this.name = name;
        this.ingredientsJson = ingredientsJson;
        this.basePortion = basePortion;
    }
}
