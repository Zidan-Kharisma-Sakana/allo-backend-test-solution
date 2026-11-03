package com.allobank.splitbill.service.split;

import static com.allobank.splitbill.exception.ApplicationException.invalidRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EqualSplitCalculator implements SplitCalculator {

    @Override
    public List<BigDecimal> split(BigDecimal amount, int participantCount) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0
                || participantCount <= 0) {
            throw invalidRequest();
        }
        amount = amount.setScale(0, RoundingMode.UNNECESSARY);
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal share = amount.divide(BigDecimal.valueOf(participantCount), 0, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(share.multiply(BigDecimal.valueOf(participantCount)));
        int remainingRupiah = remainder.intValueExact();
        for (int i = 0; i < participantCount; i++) {
            BigDecimal part = share;
            if (i < remainingRupiah) {
                part = part.add(BigDecimal.ONE);
            }
            result.add(part);
        }
        return result;
    }
}
