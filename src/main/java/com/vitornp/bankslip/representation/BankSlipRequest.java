package com.vitornp.bankslip.representation;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BankSlipRequest {

    @NotNull
    @Future
    private LocalDate dueDate;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal totalInCents;

    @NotBlank
    private String customer;

}
