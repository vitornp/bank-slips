package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipDetail;
import com.vitornp.bankslip.dto.BankSlipStatus;
import com.vitornp.bankslip.exception.BankSlipCanceledException;
import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.vitornp.bankslip.dto.BankSlipStatus.CANCELED;
import static com.vitornp.bankslip.dto.BankSlipStatus.PAID;
import static com.vitornp.bankslip.dto.BankSlipStatus.PENDING;
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

    @Test
    void cancelById() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(eq(id))).thenReturn(Optional.of(BankSlip.builder().build()));

        // When
        service.cancelById(id);

        // Then
        verify(repository).findById(eq(id));
        verify(repository).updateStatus(eq(id), eq(CANCELED));
    }

    @Test
    void cancelByIdWhenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(eq(id))).thenReturn(Optional.empty());

        // When
        Throwable exception = assertThrows(BankSlipNotFoundException.class, () -> service.cancelById(id));

        // Then
        verify(repository).findById(eq(id));
        verifyNoMoreInteractions(repository);
        assertEquals(format("Bank slip '%s' not found", id), exception.getMessage());
    }

    @Test
    void cancelByIdWhenCanNotBeCanceled() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(eq(id))).thenReturn(Optional.of(BankSlip.builder().status(PAID).build()));

        // When
        Throwable exception = assertThrows(BankSlipCanceledException.class, () -> service.cancelById(id));

        // Then
        verify(repository).findById(eq(id));
        verifyNoMoreInteractions(repository);
        assertEquals(format("Bank slip '%s' can not be canceled", id), exception.getMessage());
    }

    @Test
    void findDetailByIdWhenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(eq(id))).thenReturn(Optional.empty());

        // When
        Throwable exception = assertThrows(BankSlipNotFoundException.class, () -> service.findDetailById(id));

        // Then
        verify(repository).findById(eq(id));
        verifyNoMoreInteractions(repository);
        assertEquals(format("Bank slip '%s' not found", id), exception.getMessage());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsNow() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PENDING, LocalDate.now());
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsFiveDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PENDING, LocalDate.now().minusDays(5));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsTenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PENDING, LocalDate.now().minusDays(10));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsElevenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PENDING, LocalDate.now().minusDays(11));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("14.90"), bankSlipDetail.getFine());
    }


    @Test
    void findDetailByIdWhenPaidAndDueDateIsNow() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PAID, LocalDate.now(), LocalDate.now());
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsFiveDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PAID, LocalDate.now(), LocalDate.now().plusDays(5));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsTenDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PAID, LocalDate.now(), LocalDate.now().plusDays(10));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsElevenDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, PAID, LocalDate.now(), LocalDate.now().plusDays(11));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("14.90"), bankSlipDetail.getFine());
    }


    @Test
    void findDetailByIdWhenCanceledAndDueDateIsNow() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, CANCELED, LocalDate.now());
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenCanceledAndDueDateIsFiveDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, CANCELED, LocalDate.now().plusDays(10));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenCanceledAndDueDateIsTenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = givenBankSlip(id, CANCELED, LocalDate.now().plusDays(11));
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    private BankSlip givenBankSlip(UUID id, BankSlipStatus status, LocalDate dueDate) {
        return givenBankSlip(id, status, dueDate, null);
    }

    private BankSlip givenBankSlip(UUID id, BankSlipStatus status, LocalDate dueDate, LocalDate paymentDate) {
        return BankSlip.builder()
            .id(id)
            .dueDate(dueDate)
            .paymentDate(paymentDate)
            .totalInCents(new BigDecimal("1490.13"))
            .customer("Test")
            .status(status)
            .build();
    }

    private void assertEqualsBankSlipDetail(BankSlip expected, BankSlipDetail actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDueDate(), actual.getDueDate());
        assertEquals(expected.getPaymentDate(), actual.getPaymentDate());
        assertEquals(expected.getTotalInCents(), actual.getTotalInCents());
        assertEquals(expected.getCustomer(), actual.getCustomer());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt());
    }
}
