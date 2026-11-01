package com.partner.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanPaymentCheckPaymentStatusBranchTest {

    private static LoanPayment payment(
            BigDecimal due, BigDecimal paid, LocalDate dueDate) {
        return new LoanPayment("p1", due, paid, dueDate, null);
    }

    @Test
    // covers Path 1: <N1, N2, N3, N9>
    void fullPaymentMarksPaidRegardlessOfDueDate() {
        LoanPayment payment = payment(
                new BigDecimal("200.00"), new BigDecimal("200.00"), LocalDate.of(2026, 1, 1));

        PaymentStatus result = payment.checkPaymentStatus(LocalDate.of(2026, 3, 1));

        assertEquals(PaymentStatus.PAID, result);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
    }

    @Test
    // covers Path 2: <N1, N2, N4, N5, N9>
    void notYetDueAndUnderpaidStaysPending() {
        LoanPayment payment = payment(
                new BigDecimal("150.00"), new BigDecimal("0.00"), LocalDate.of(2026, 5, 15));

        PaymentStatus result = payment.checkPaymentStatus(LocalDate.of(2026, 5, 15));

        assertEquals(PaymentStatus.PENDING, result);
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    // covers Path 3: <N1, N2, N4, N6, N7, N9>
    void partialPaymentAfterDueDate() {
        LoanPayment payment = payment(
                new BigDecimal("300.00"), new BigDecimal("50.00"), LocalDate.of(2026, 2, 1));

        PaymentStatus result = payment.checkPaymentStatus(LocalDate.of(2026, 2, 10));

        assertEquals(PaymentStatus.PARTIALLY_PAID_LATE, result);
        assertEquals(PaymentStatus.PARTIALLY_PAID_LATE, payment.getStatus());
    }

    @Test
    // covers Path 4: <N1, N2, N4, N6, N8, N9>
    void noPaymentAfterDueDate() {
        LoanPayment payment = payment(
                new BigDecimal("125.00"), BigDecimal.ZERO, LocalDate.of(2026, 4, 1));

        PaymentStatus result = payment.checkPaymentStatus(LocalDate.of(2026, 4, 2));

        assertEquals(PaymentStatus.MISSED, result);
        assertEquals(PaymentStatus.MISSED, payment.getStatus());
    }
}
