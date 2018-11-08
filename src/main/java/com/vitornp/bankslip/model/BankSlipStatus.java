package com.vitornp.bankslip.model;

import com.vitornp.bankslip.dto.BankSlipStatusValue;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
public class BankSlipStatus {

    @Default
    private UUID id = UUID.randomUUID();

    private UUID bankSlipId;

    @Default
    private LocalDate date = LocalDate.now();

    @Default
    private BankSlipStatusValue status = BankSlipStatusValue.PENDING;

    @Default
    private Instant createdAt = Instant.now();

}
