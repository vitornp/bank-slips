package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipDetail;
import com.vitornp.bankslip.exception.BankSlipCanceledException;
import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.vitornp.bankslip.dto.BankSlipStatus.CANCELED;
import static com.vitornp.bankslip.dto.BankSlipStatus.PAID;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.time.temporal.ChronoUnit.DAYS;

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

    public BankSlipDetail findDetailById(UUID id) {
        BankSlip bankSlip = this.findById(id);
        BigDecimal fine = getFine(bankSlip);

        return BankSlipDetail.builder()
            .id(bankSlip.getId())
            .dueDate(bankSlip.getDueDate())
            .paymentDate(bankSlip.getPaymentDate())
            .totalInCents(bankSlip.getTotalInCents())
            .customer(bankSlip.getCustomer())
            .status(bankSlip.getStatus())
            .createdAt(bankSlip.getCreatedAt())
            .updatedAt(bankSlip.getUpdatedAt())
            .fine(fine)
            .build();
    }

    private BigDecimal getFine(BankSlip bankSlip) {
        LocalDate paymentDate = getPaymentDate(bankSlip);

        long daysBetween = DAYS.between(bankSlip.getDueDate(), paymentDate);

        BigDecimal rate = BigDecimal.ZERO;
        if (daysBetween > 10) {
            rate = new BigDecimal("0.01");
        } else if (daysBetween > 0) {
            rate = new BigDecimal("0.005");
        }

        BigDecimal totalInCents = bankSlip.getTotalInCents();

        return totalInCents.multiply(rate).setScale(2, ROUND_HALF_UP);
    }

    private LocalDate getPaymentDate(BankSlip bankSlip) {
        switch (bankSlip.getStatus()) {
            case PAID:
                return bankSlip.getPaymentDate();
            case CANCELED:
                return bankSlip.getDueDate();
            case PENDING:
            default:
                return LocalDate.now();
        }
    }

    private BankSlip findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new BankSlipNotFoundException(id));
    }

}
