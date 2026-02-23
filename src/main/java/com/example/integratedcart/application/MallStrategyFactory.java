package com.example.integratedcart.application;

import com.example.integratedcart.domain.product.MallType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MallStrategyFactory {

    private final Map<MallType, MallStrategy> strategies;

    public MallStrategyFactory(List<MallStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(MallStrategy::getMallType, Function.identity()));
    }

    public MallStrategy getStrategy(MallType type) {
        MallStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown MallType: " + type);
        }
        return strategy;
    }
}
