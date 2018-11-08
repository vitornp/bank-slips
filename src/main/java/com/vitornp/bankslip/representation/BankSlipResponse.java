package com.vitornp.bankslip.representation;

import com.vitornp.bankslip.dto.BankSlipStatusValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BankSlipResponse {
    private UUID id;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private BigDecimal totalInCents;
    private String customer;
    private BankSlipStatusValue status;
    private BigDecimal fine;
}
