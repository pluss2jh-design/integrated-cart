package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 쇼핑몰 타입에 맞는 전략(Strategy) 객체를 제공하는 팩토리.
 */
 @Component
public class MallStrategyFactory {

    private final Map<MallType, MallStrategy> strategies;

    public MallStrategyFactory(List<MallStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(MallStrategy::getMallType, Function.identity()));
    }

    /**
     * 지정된 쇼핑몰 타입에 해당하는 전략을 반환합니다.
     * @param type 쇼핑몰 타입
     * @return 해당 쇼핑몰의 검색 전략
     * @throws IllegalArgumentException 알 수 없는 쇼핑몰 타입인 경우
     */
    public MallStrategy getStrategy(MallType type) {
        MallStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("알 수 없는 쇼핑몰 타입: " + type);
        }
        return strategy;
    }
}
