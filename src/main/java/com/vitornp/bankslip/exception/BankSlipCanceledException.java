package com.vitornp.bankslip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import static java.lang.String.format;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BankSlipCanceledException extends RuntimeException {

    public BankSlipCanceledException(UUID id) {
        super(format("Bank slip '%s' can not be canceled", id));
    }

}
