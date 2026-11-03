package com.allobank.splitbill.service.split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EqualSplitCalculator implements SplitCalculator {

    @Override
    public List<BigDecimal> split(BigDecimal amount, int participantCount) {
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal share = amount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(share.multiply(BigDecimal.valueOf(participantCount)));
        int remainingCents = remainder.movePointRight(2).intValueExact();
        for (int i = 0; i < participantCount; i++) {
            BigDecimal part = share;
            if (i < remainingCents) {
                part = part.add(new BigDecimal("0.01"));
            }
            result.add(part);
        }
        return result;
    }
}
