package com.vitornp.bankslip.representation;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class BankSlipPaymentRequest {

    @NotNull
    @Future
    private LocalDate paymentDate;

}
