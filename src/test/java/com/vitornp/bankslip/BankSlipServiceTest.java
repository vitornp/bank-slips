package com.vitornp.bankslip;

import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankSlipServiceTest {

    @InjectMocks
    private BankSlipService service;

    @Mock
    private BankSlipRepository repository;

    @Test
    void paymentById() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate paymentDate = LocalDate.now();
        when(repository.findById(eq(id))).thenReturn(Optional.of(BankSlip.builder().build()));

        // When
        service.paymentById(id, paymentDate);

        // Then
        verify(repository).findById(eq(id));
        verify(repository).updatePayment(eq(id), eq(paymentDate));
    }

    @Test
    void paymentByIdWhenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDate paymentDate = LocalDate.now();
        when(repository.findById(eq(id))).thenReturn(Optional.empty());

        // When
        Throwable exception = assertThrows(BankSlipNotFoundException.class, () -> service.paymentById(id, paymentDate));

        // Then
        verify(repository).findById(eq(id));
        verifyNoMoreInteractions(repository);
        assertEquals(format("Bank slip '%s' not found", id), exception.getMessage());
    }

}
