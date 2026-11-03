package com.allobank.splitbill;

import com.allobank.splitbill.exception.ApplicationException;
import com.allobank.splitbill.service.split.EqualSplitCalculator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EqualSplitCalculatorTest {
    private final EqualSplitCalculator calculator = new EqualSplitCalculator();

    @Test
    void splitRupiah() {
        assertEquals(List.of(new BigDecimal("3334"), new BigDecimal("3333"), new BigDecimal("3333")),
                calculator.split(new BigDecimal("10000"), 3));
        assertEquals(List.of(new BigDecimal("3000"), new BigDecimal("3000"), new BigDecimal("3000")),
                calculator.split(new BigDecimal("9000.00"), 3));
        assertEquals(List.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO),
                calculator.split(new BigDecimal("2"), 3));
    }

    @Test
    void preserveTotal() {
        for (int amount = 1; amount <= 100; amount++) {
            for (int count = 1; count <= 12; count++) {
                var shares = calculator.split(BigDecimal.valueOf(amount), count);
                assertEquals(BigDecimal.valueOf(amount), shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                assertTrue(shares.stream().allMatch(share -> share.signum() >= 0 && share.scale() == 0));
                assertTrue(shares.getFirst().subtract(shares.getLast()).compareTo(BigDecimal.ONE) <= 0);
            }
        }
    }

    @Test
    void invalidInput() {
        for (String amount : List.of("0", "-1", "10.50")) {
            assertThrows(ApplicationException.class, () -> calculator.split(new BigDecimal(amount), 3));
        }
        assertThrows(ApplicationException.class, () -> calculator.split(BigDecimal.ONE, 0));
    }
}
