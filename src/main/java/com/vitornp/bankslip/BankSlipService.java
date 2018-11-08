package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipDetail;
import com.vitornp.bankslip.dto.BankSlipStatusValue;
import com.vitornp.bankslip.exception.BankSlipCanceledException;
import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import com.vitornp.bankslip.model.BankSlipStatus;
import com.vitornp.bankslip.repository.BankSlipRepository;
import com.vitornp.bankslip.repository.BankSlipStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vitornp.bankslip.dto.BankSlipStatusValue.CANCELED;
import static com.vitornp.bankslip.dto.BankSlipStatusValue.PAID;
import static com.vitornp.bankslip.dto.BankSlipStatusValue.PENDING;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.singletonList;

@Service
public class BankSlipService {

    private final BankSlipRepository repository;
    private final BankSlipStatusRepository statusRepository;

    @Autowired
    public BankSlipService(BankSlipRepository repository, BankSlipStatusRepository statusRepository) {
        this.repository = repository;
        this.statusRepository = statusRepository;
    }

    @Transactional
    public BankSlip save(BankSlip bankSlip) {
        BankSlip bankSlipSaved = repository.save(bankSlip);
        BankSlipStatus bankSlipStatus = saveBankSlipStatus(bankSlipSaved.getId(), LocalDate.now(), PENDING);

        return bankSlipSaved.toBuilder()
            .statuses(singletonList(bankSlipStatus))
            .build();
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public List<BankSlip> findAll() {
        return repository.findAll()
            .stream()
            .map(bankSlip -> bankSlip.toBuilder()
                .statuses(statusRepository.findAllByBankSlipId(bankSlip.getId()))
                .build()
            )
            .collect(Collectors.toList());
    }

    @Transactional
    public void paymentById(UUID id, LocalDate paymentDate) {
        this.findById(id);
        saveBankSlipStatus(id, paymentDate, PAID);
    }

    @Transactional
    public void cancelById(UUID id) {
        BankSlip bankSlip = this.findById(id);

        if (PAID == bankSlip.getLastStatus().getStatus()) {
            throw new BankSlipCanceledException(id);
        }

        saveBankSlipStatus(id, LocalDate.now(), CANCELED);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, readOnly = true)
    public BankSlipDetail findDetailById(UUID id) {
        BankSlip bankSlip = this.findById(id);
        BigDecimal fine = getFine(bankSlip);

        LocalDate paymentDate = bankSlip.getPaidStatus()
            .map(BankSlipStatus::getDate)
            .orElse(null);

        return BankSlipDetail.builder()
            .id(bankSlip.getId())
            .dueDate(bankSlip.getDueDate())
            .paymentDate(paymentDate)
            .totalInCents(bankSlip.getTotalInCents())
            .customer(bankSlip.getCustomer())
            .status(bankSlip.getLastStatus().getStatus())
            .createdAt(bankSlip.getCreatedAt())
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
        BankSlipStatus lastStatus = bankSlip.getLastStatus();
        switch (lastStatus.getStatus()) {
            case PAID:
                return lastStatus.getDate();
            case CANCELED:
                return bankSlip.getDueDate();
            case PENDING:
            default:
                return LocalDate.now();
        }
    }

    private BankSlip findById(UUID id) {
        BankSlip bankSlip = repository.findById(id).orElseThrow(() -> new BankSlipNotFoundException(id));
        List<BankSlipStatus> statuses = statusRepository.findAllByBankSlipId(bankSlip.getId());
        return bankSlip.toBuilder().statuses(statuses).build();
    }

    private BankSlipStatus saveBankSlipStatus(UUID bankSlipId, LocalDate date, BankSlipStatusValue status) {
        BankSlipStatus bankSlipStatus = BankSlipStatus.builder()
            .bankSlipId(bankSlipId)
            .date(date)
            .status(status)
            .build();
        statusRepository.save(bankSlipStatus);
        return bankSlipStatus;
    }

}
