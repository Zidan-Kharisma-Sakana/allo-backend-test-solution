package com.allobank.splitbill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        String currency,
        BigDecimal totalExpenses,
        List<Balance> balances,
        List<Transfer> settlements,
        @JsonProperty("service_charge_pct") int serviceChargePct,
        @JsonProperty("service_charge_amount") BigDecimal serviceChargeAmount) {

    public record Balance(UUID participantId, String name, BigDecimal paid, BigDecimal owed, BigDecimal balance) {}

    public record Transfer(UUID fromParticipantId, UUID toParticipantId, BigDecimal amount) {}
}
