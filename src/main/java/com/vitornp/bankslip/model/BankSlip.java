package com.vitornp.bankslip.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.vitornp.bankslip.dto.BankSlipStatusValue.PAID;

@Builder(toBuilder = true)
@Getter
public class BankSlip {

    @Default
    private UUID id = UUID.randomUUID();

    private LocalDate dueDate;

    private BigDecimal totalInCents;

    private String customer;

    @Default
    private List<BankSlipStatus> statuses = new ArrayList<>();

    @Default
    private Instant createdAt = Instant.now();

    public BankSlipStatus getLastStatus() {
        return statuses.stream()
            .max(Comparator.comparing(BankSlipStatus::getCreatedAt))
            .orElse(BankSlipStatus.builder().bankSlipId(id).build());
    }

    public Optional<BankSlipStatus> getPaidStatus() {
        return statuses.stream()
            .filter(p -> PAID == p.getStatus())
            .findFirst();
    }

}
