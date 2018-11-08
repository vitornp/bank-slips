package com.vitornp.bankslip.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
public class BankSlipDetail {
    private UUID id;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private BigDecimal totalInCents;
    private String customer;
    private BankSlipStatusValue status;
    private Instant createdAt;
    private BigDecimal fine;
}
