package com.vitornp.bankslip;

import com.vitornp.bankslip.exception.BankSlipCanceledException;
import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.vitornp.bankslip.dto.BankSlipStatus.CANCELED;
import static com.vitornp.bankslip.dto.BankSlipStatus.PAID;

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

    public void cancelById(UUID id) {
        BankSlip bankSlip = this.findById(id);

        if (PAID == bankSlip.getStatus()) {
            throw new BankSlipCanceledException(id);
        }

        repository.updateStatus(id, CANCELED);
    }

    private BankSlip findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new BankSlipNotFoundException(id));
    }

}
