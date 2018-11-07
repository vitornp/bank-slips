package com.vitornp.bankslip;

import com.vitornp.bankslip.model.BankSlip;
import com.vitornp.bankslip.representation.BankSlipResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BankSlipService {

    private final BankSlipRepository repository;

    @Autowired
    public BankSlipService(BankSlipRepository repository) {
        this.repository = repository;
    }

    public BankSlip save(BankSlip bankSlip) {
        return repository.save(bankSlip);
    }

    public List<BankSlip> findAll() {
        return repository.findAll();
    }

    public void paymentById(UUID id, LocalDate paymentDate) {
        this.findById(id);
        repository.updatePayment(id, paymentDate);
    }

    private BankSlip findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new BankSlipNotFoundException(id));
    }

}
