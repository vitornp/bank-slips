package com.vitornp.bankslip;

import com.vitornp.bankslip.model.BankSlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}