package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipStatus;
import com.vitornp.bankslip.model.BankSlip;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BankSlipRepositoryIT {

    @Autowired
    private BankSlipRepository repository;

    @Test
    void updatePayment() {
        // Given a bank slip
        BankSlip bankSlipCreated = givenBankSlip();
        repository.save(bankSlipCreated);

        // Given data for update
        UUID id = bankSlipCreated.getId();
        LocalDate paymentDate = LocalDate.now().plusDays(1);

        // When
        repository.updatePayment(id, paymentDate);

        // Then
        Optional<BankSlip> bankSlipOptional = repository.findById(id);
        assertTrue(bankSlipOptional.isPresent());

        BankSlip bankSlip = bankSlipOptional.get();
        assertEquals(BankSlipStatus.PAID, bankSlip.getStatus());
        assertEquals(paymentDate, bankSlip.getPaymentDate());
        assertTrue(bankSlipCreated.getUpdatedAt().isBefore(bankSlip.getUpdatedAt()));
    }

    @Test
    void updateStatus() {
        // Given a bank slip
        BankSlip bankSlipCreated = givenBankSlip();
        repository.save(bankSlipCreated);

        // Given data for update
        UUID id = bankSlipCreated.getId();
        BankSlipStatus bankSlipStatus = BankSlipStatus.CANCELED;

        // When
        repository.updateStatus(id, bankSlipStatus);

        // Then
        Optional<BankSlip> bankSlipOptional = repository.findById(id);
        assertTrue(bankSlipOptional.isPresent());

        BankSlip bankSlip = bankSlipOptional.get();
        assertEquals(bankSlipStatus, bankSlip.getStatus());
        assertTrue(bankSlipCreated.getUpdatedAt().isBefore(bankSlip.getUpdatedAt()));
    }

    private BankSlip givenBankSlip() {
        return BankSlip.builder()
            .dueDate(LocalDate.now())
            .customer("Test")
            .totalInCents(new BigDecimal(0.1))
            .build();
    }
}
