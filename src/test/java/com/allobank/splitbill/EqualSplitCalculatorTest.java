package com.allobank.splitbill;

import com.allobank.splitbill.service.EqualSplitCalculator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EqualSplitCalculatorTest {

    @Test
    void sharesKeepTheRemainingCent() {
        List<BigDecimal> shares = new EqualSplitCalculator().split(new BigDecimal("100.00"), 3);
        assertEquals(List.of(new BigDecimal("33.34"), new BigDecimal("33.33"), new BigDecimal("33.33")), shares);
    }
}
