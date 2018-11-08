package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipDetail;
import com.vitornp.bankslip.model.BankSlip;
import com.vitornp.bankslip.representation.BankSlipPaymentRequest;
import com.vitornp.bankslip.representation.BankSlipRequest;
import com.vitornp.bankslip.representation.BankSlipResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping
    public List<BankSlipResponse> findAll() {
        return service.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public BankSlipResponse findById(@PathVariable UUID id) {
        return toResponse(service.findDetailById(id));
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void paymentById(@PathVariable UUID id, @RequestBody @Valid BankSlipPaymentRequest request) {
        service.paymentById(id, request.getPaymentDate());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelById(@PathVariable UUID id) {
        service.cancelById(id);
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

    private BankSlipResponse toResponse(BankSlipDetail bankSlipDetail) {
        return BankSlipResponse.builder()
            .id(bankSlipDetail.getId())
            .dueDate(bankSlipDetail.getDueDate())
            .totalInCents(bankSlipDetail.getTotalInCents())
            .customer(bankSlipDetail.getCustomer())
            .status(bankSlipDetail.getStatus())
            .fine(bankSlipDetail.getFine())
            .build();
    }

}
