package com.vitornp.bankslip;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import static java.lang.String.format;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BankSlipNotFoundException extends RuntimeException {

    public BankSlipNotFoundException(UUID id) {
        super(format("Bank slip '%s' not found", id));
    }

}
