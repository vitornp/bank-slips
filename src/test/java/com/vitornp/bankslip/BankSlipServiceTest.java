package com.vitornp.bankslip;

import com.vitornp.bankslip.dto.BankSlipDetail;
import com.vitornp.bankslip.dto.BankSlipStatusValue;
import com.vitornp.bankslip.exception.BankSlipCanceledException;
import com.vitornp.bankslip.exception.BankSlipNotFoundException;
import com.vitornp.bankslip.model.BankSlip;
import com.vitornp.bankslip.model.BankSlipStatus;
import com.vitornp.bankslip.repository.BankSlipRepository;
import com.vitornp.bankslip.repository.BankSlipStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.vitornp.bankslip.dto.BankSlipStatusValue.CANCELED;
import static com.vitornp.bankslip.dto.BankSlipStatusValue.PAID;
import static com.vitornp.bankslip.dto.BankSlipStatusValue.PENDING;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Mock
    private BankSlipStatusRepository statusRepository;

    @Captor
    private ArgumentCaptor<BankSlipStatus> bankSlipStatusCaptor;

    @Test
    void paymentById() {
        // Given
        UUID bankSlipId = UUID.randomUUID();
        LocalDate paymentDate = LocalDate.now();
        when(repository.findById(eq(bankSlipId))).thenReturn(Optional.of(BankSlip.builder().id(bankSlipId).build()));

        // When
        service.paymentById(bankSlipId, paymentDate);

        // Then
        verify(repository).findById(eq(bankSlipId));
        verify(statusRepository).findAllByBankSlipId(eq(bankSlipId));
        verify(statusRepository).save(bankSlipStatusCaptor.capture());
        BankSlipStatus bankSlipStatus = bankSlipStatusCaptor.getValue();
        assertNotNull(bankSlipStatus.getId());
        assertEquals(bankSlipId, bankSlipStatus.getBankSlipId());
        assertEquals(paymentDate, bankSlipStatus.getDate());
        assertEquals(PAID, bankSlipStatus.getStatus());
        assertNotNull(bankSlipStatus.getCreatedAt());
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
        verifyNoMoreInteractions(repository, statusRepository);
        assertEquals(format("Bank slip '%s' not found", id), exception.getMessage());
    }

    @Test
    void cancelById() {
        // Given
        UUID bankSlipId = UUID.randomUUID();
        when(repository.findById(eq(bankSlipId))).thenReturn(Optional.of(BankSlip.builder().id(bankSlipId).build()));

        // When
        service.cancelById(bankSlipId);

        // Then
        verify(repository).findById(eq(bankSlipId));
        verify(statusRepository).findAllByBankSlipId(eq(bankSlipId));
        verify(statusRepository).save(bankSlipStatusCaptor.capture());
        BankSlipStatus bankSlipStatus = bankSlipStatusCaptor.getValue();
        assertNotNull(bankSlipStatus.getId());
        assertEquals(bankSlipId, bankSlipStatus.getBankSlipId());
        assertEquals(LocalDate.now(), bankSlipStatus.getDate());
        assertEquals(CANCELED, bankSlipStatus.getStatus());
        assertNotNull(bankSlipStatus.getCreatedAt());
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
        verifyNoMoreInteractions(repository, statusRepository);
        assertEquals(format("Bank slip '%s' not found", id), exception.getMessage());
    }

    @Test
    void cancelByIdWhenCanNotBeCanceled() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PAID);

        // When
        Throwable exception = assertThrows(BankSlipCanceledException.class, () -> service.cancelById(id));

        // Then
        verify(repository).findById(eq(id));
        verify(statusRepository).findAllByBankSlipId(eq(id));
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
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PENDING);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsFiveDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now().minusDays(5));
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PENDING);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsTenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now().minusDays(10));
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PENDING);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPendingAndDueDateIsElevenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now().minusDays(11));
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PENDING);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("14.90"), bankSlipDetail.getFine());
    }


    @Test
    void findDetailByIdWhenPaidAndDueDateIsNow() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PAID, LocalDate.now());

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsFiveDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PAID, LocalDate.now().plusDays(5));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsTenDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PAID, LocalDate.now().plusDays(10));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("7.45"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenPaidAndDueDateIsElevenDaysAhead() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, PAID, LocalDate.now().plusDays(11));

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("14.90"), bankSlipDetail.getFine());
    }


    @Test
    void findDetailByIdWhenCanceledAndDueDateIsNow() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now());
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, CANCELED);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenCanceledAndDueDateIsFiveDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now().plusDays(10));
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, CANCELED);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    @Test
    void findDetailByIdWhenCanceledAndDueDateIsTenDaysAgo() {
        // Given
        UUID id = UUID.randomUUID();
        BankSlip bankSlip = mockBankSlip(id, LocalDate.now().plusDays(11));
        BankSlipStatus bankSlipStatus = mockBankSlipStatus(id, CANCELED);

        // When
        BankSlipDetail bankSlipDetail = service.findDetailById(id);

        // Then
        verify(repository).findById(eq(id));
        assertEqualsBankSlipDetail(bankSlip, bankSlipStatus, bankSlipDetail);
        assertEquals(new BigDecimal("0.00"), bankSlipDetail.getFine());
    }

    private BankSlip mockBankSlip(UUID id, LocalDate dueDate) {
        BankSlip bankSlip = BankSlip.builder()
            .id(id)
            .dueDate(dueDate)
            .totalInCents(new BigDecimal("1490.13"))
            .customer("Test")
            .build();
        when(repository.findById(eq(id))).thenReturn(Optional.of(bankSlip));
        return bankSlip;
    }

    private BankSlipStatus mockBankSlipStatus(UUID bankSlipId, BankSlipStatusValue status) {
        return mockBankSlipStatus(bankSlipId, status, null);
    }

    private BankSlipStatus mockBankSlipStatus(UUID bankSlipId, BankSlipStatusValue status, LocalDate paymentDate) {
        BankSlipStatus bankSlipStatus = BankSlipStatus.builder()
            .bankSlipId(bankSlipId)
            .status(status)
            .date(paymentDate)
            .build();

        when(statusRepository.findAllByBankSlipId(eq(bankSlipId))).thenReturn(singletonList(bankSlipStatus));
        return bankSlipStatus;
    }

    private void assertEqualsBankSlipDetail(BankSlip expected, BankSlipStatus expectedStatus, BankSlipDetail actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDueDate(), actual.getDueDate());
        assertEquals(expected.getTotalInCents(), actual.getTotalInCents());
        assertEquals(expected.getCustomer(), actual.getCustomer());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expectedStatus.getStatus(), actual.getStatus());
        assertEquals(expectedStatus.getDate(), actual.getPaymentDate());
    }
}
