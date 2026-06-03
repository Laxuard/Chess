package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TwoFactorStrategyFactory {

    private final Map<TwoFactorMethodType, TwoFactorStrategy> strategies;

    public TwoFactorStrategyFactory(List<TwoFactorStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(TwoFactorStrategy::getType, strategy -> strategy));
    }

    public TwoFactorStrategy getStrategy(TwoFactorMethodType type) {
        TwoFactorStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported 2FA strategy handler for type: " + type);
        }
        return strategy;
    }
}