package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateExpenseRequest(UUID payerId, BigDecimal amount, String description, List<UUID> participantIds,
                                   Map<UUID, BigDecimal> allocations) {}
