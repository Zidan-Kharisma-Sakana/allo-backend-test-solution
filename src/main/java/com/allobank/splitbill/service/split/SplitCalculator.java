package com.allobank.splitbill.service.split;

import static com.allobank.splitbill.exception.ApplicationException.invalidRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SplitCalculator {

    public List<BigDecimal> splitEqual(BigDecimal amount, int participantCount) {
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

    public List<BigDecimal> splitExact(BigDecimal amount, List<UUID> participantIds,
                                       Map<UUID, BigDecimal> allocations) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0
                || participantIds == null || participantIds.isEmpty() || participantIds.stream().anyMatch(id -> id == null)
                || allocations == null || new HashSet<>(participantIds).size() != participantIds.size()
                || !allocations.keySet().equals(new HashSet<>(participantIds))) {
            throw invalidRequest();
        }
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (UUID participantId : participantIds) {
            BigDecimal allocation = allocations.get(participantId);
            if (allocation == null || allocation.signum() < 0 || allocation.stripTrailingZeros().scale() > 0) {
                throw invalidRequest();
            }
            allocation = allocation.setScale(0, RoundingMode.UNNECESSARY);
            result.add(allocation);
            total = total.add(allocation);
        }
        if (total.compareTo(amount) != 0) {
            throw invalidRequest();
        }
        return result;
    }
}
