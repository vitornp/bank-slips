package com.vitornp.bankslip;

import com.vitornp.bankslip.model.BankSlip;
import com.vitornp.bankslip.representation.BankSlipRequest;
import com.vitornp.bankslip.representation.BankSlipResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "bankslips", produces = APPLICATION_JSON_VALUE)
public class BankSlipController {

    private final BankSlipService service;

    @Autowired
    public BankSlipController(BankSlipService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankSlipResponse create(@RequestBody @Valid BankSlipRequest request) {
        BankSlip save = service.save(toModel(request));
        return toResponse(save);
    }

    private BankSlip toModel(BankSlipRequest request) {
        return BankSlip.builder()
            .dueDate(request.getDueDate())
            .totalInCents(request.getTotalInCents())
            .customer(request.getCustomer())
            .build();
    }

    private BankSlipResponse toResponse(BankSlip bankSlip) {
        return BankSlipResponse.builder()
            .id(bankSlip.getId())
            .dueDate(bankSlip.getDueDate())
            .totalInCents(bankSlip.getTotalInCents())
            .customer(bankSlip.getCustomer())
            .status(bankSlip.getStatus())
            .build();
    }

}
