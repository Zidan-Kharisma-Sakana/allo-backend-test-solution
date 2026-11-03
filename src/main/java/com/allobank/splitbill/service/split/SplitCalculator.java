package com.allobank.splitbill.service.split;

import java.math.BigDecimal;
import java.util.List;

public interface SplitCalculator {

    List<BigDecimal> split(BigDecimal amount, int participantCount);
}
