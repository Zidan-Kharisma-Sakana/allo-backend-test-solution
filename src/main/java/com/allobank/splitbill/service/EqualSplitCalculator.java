package com.allobank.splitbill.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EqualSplitCalculator {

    public List<BigDecimal> split(BigDecimal amount, int participantCount) {
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal share = amount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
        for (int i = 0; i < participantCount; i++) {
            result.add(share);
        }
        return result;
    }
}
