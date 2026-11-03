package com.allobank.splitbill;

import com.allobank.splitbill.exception.ApplicationException;
import com.allobank.splitbill.service.split.SplitCalculator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitCalculatorTest {
    private final SplitCalculator calculator = new SplitCalculator();

    @Test
    void exactShares() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        UUID cara = UUID.randomUUID();
        var allocations = Map.of(alice, new BigDecimal("50000.00"),
                bob, new BigDecimal("30000"), cara, new BigDecimal("20000"));
        assertEquals(List.of(new BigDecimal("20000"), new BigDecimal("50000"), new BigDecimal("30000")),
                calculator.splitExact(new BigDecimal("100000"), List.of(cara, alice, bob), allocations));
        assertEquals(List.of(BigDecimal.ZERO, BigDecimal.ONE),
                calculator.splitExact(BigDecimal.ONE, List.of(alice, bob),
                        Map.of(alice, BigDecimal.ZERO, bob, BigDecimal.ONE)));
    }

    @Test
    void invalidExactShares() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        for (String allocation : List.of("9", "11", "-10", "10.50")) {
            assertThrows(ApplicationException.class, () -> calculator.splitExact(new BigDecimal("10"),
                    List.of(alice), Map.of(alice, new BigDecimal(allocation))));
        }
        assertThrows(ApplicationException.class, () -> calculator.splitExact(BigDecimal.ONE,
                List.of(alice), Map.of(bob, BigDecimal.ONE)));
        assertThrows(ApplicationException.class, () -> calculator.splitExact(BigDecimal.ONE,
                List.of(alice, bob), Map.of(alice, BigDecimal.ONE)));
        var missing = new HashMap<UUID, BigDecimal>();
        missing.put(alice, null);
        assertThrows(ApplicationException.class, () -> calculator.splitExact(BigDecimal.ONE, List.of(alice), missing));
    }

    @Test
    void splitRupiah() {
        assertEquals(List.of(new BigDecimal("3334"), new BigDecimal("3333"), new BigDecimal("3333")),
                calculator.splitEqual(new BigDecimal("10000"), 3));
        assertEquals(List.of(new BigDecimal("3000"), new BigDecimal("3000"), new BigDecimal("3000")),
                calculator.splitEqual(new BigDecimal("9000.00"), 3));
        assertEquals(List.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO),
                calculator.splitEqual(new BigDecimal("2"), 3));
    }

    @Test
    void preserveTotal() {
        for (int amount = 1; amount <= 100; amount++) {
            for (int count = 1; count <= 12; count++) {
                var shares = calculator.splitEqual(BigDecimal.valueOf(amount), count);
                assertEquals(BigDecimal.valueOf(amount), shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                assertTrue(shares.stream().allMatch(share -> share.signum() >= 0 && share.scale() == 0));
                assertTrue(shares.getFirst().subtract(shares.getLast()).compareTo(BigDecimal.ONE) <= 0);
            }
        }
    }

    @Test
    void invalidInput() {
        for (String amount : List.of("0", "-1", "10.50")) {
            assertThrows(ApplicationException.class, () -> calculator.splitEqual(new BigDecimal(amount), 3));
        }
        assertThrows(ApplicationException.class, () -> calculator.splitEqual(BigDecimal.ONE, 0));
    }
}
