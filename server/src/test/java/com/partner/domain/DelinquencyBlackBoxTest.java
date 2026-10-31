package com.partner.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Black-box cases derived from A2 Part 2 tables (test basis R1–R3 / I1–I2).
 */
class DelinquencyBlackBoxTest {

    private final DelinquencyPolicy policy = new DelinquencyPolicy();

    // --- Behavior 1: classify installment payment status (Basis R1 / I1) ---

    @Test
    // T1 covers P1 valid on-time unpaid
    void t1_pendingBeforeDueDate() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 8, 1), null);

        assertEquals(PaymentStatus.PENDING, payment.checkPaymentStatus(LocalDate.of(2026, 7, 31)));
    }

    @Test
    // T2 covers P2 valid missed
    void t2_missedAfterDueDateWithZeroPaid() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 8, 1), null);

        assertEquals(PaymentStatus.MISSED, payment.checkPaymentStatus(LocalDate.of(2026, 8, 2)));
    }

    @Test
    // T3 covers P3 valid partial late
    void t3_partiallyPaidLate() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), new BigDecimal("40.00"), LocalDate.of(2026, 8, 1), null);

        assertEquals(PaymentStatus.PARTIALLY_PAID_LATE, payment.checkPaymentStatus(LocalDate.of(2026, 8, 5)));
    }

    @Test
    // T4 covers P4 valid paid in full (I1: fully paid never missed)
    void t4_paidInFullAfterDueDate() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), new BigDecimal("100.00"), LocalDate.of(2026, 8, 1), null);

        assertEquals(PaymentStatus.PAID, payment.checkPaymentStatus(LocalDate.of(2026, 9, 1)));
    }

    @Test
    // T5 covers B2 (evaluation date = due date)
    void t5_onDueDateStillPending() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 5, 10), null);

        assertEquals(PaymentStatus.PENDING, payment.checkPaymentStatus(LocalDate.of(2026, 5, 10)));
    }

    @Test
    // T6 covers B3 (evaluation date = due date + 1 day)
    void t6_dayAfterDueIsMissed() {
        LoanPayment payment = new LoanPayment(
                "p1", new BigDecimal("100.00"), BigDecimal.ZERO, LocalDate.of(2026, 5, 10), null);

        assertEquals(PaymentStatus.MISSED, payment.checkPaymentStatus(LocalDate.of(2026, 5, 11)));
    }

    // --- Behavior 2: loan delinquency from six-month or 25% unpaid rules (Basis R3 / I3) ---

    @Test
    // T7 covers P1 current: late but under both thresholds
    void t7_smallMissedBalanceNotYetDelinquent() {
        LoanPayment late = new LoanPayment(
                "p1", new BigDecimal("500.00"), BigDecimal.ZERO, LocalDate.of(2025, 8, 1), null);
        Loan loan = new Loan("L-1", new BigDecimal("10000.00"), List.of(late));

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 1, 1)));
        assertFalse(loan.determineDelinquency(LocalDate.of(2026, 1, 1), policy));
    }

    @Test
    // T8 covers B1 at 25% unpaid boundary (exactly 25% is not delinquent)
    void t8_exactlyTwentyFivePercentUnpaidStillCurrent() {
        LoanPayment late = new LoanPayment(
                "p1", new BigDecimal("2500.00"), BigDecimal.ZERO, LocalDate.of(2026, 1, 1), null);
        Loan loan = new Loan("L-1", new BigDecimal("10000.00"), List.of(late));

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 2, 1)));
    }

    @Test
    // T9 covers B2 above 25% unpaid boundary
    void t9_moreThanTwentyFivePercentUnpaidIsDelinquent() {
        LoanPayment late = new LoanPayment(
                "p1", new BigDecimal("2500.01"), BigDecimal.ZERO, LocalDate.of(2026, 1, 1), null);
        Loan loan = new Loan("L-1", new BigDecimal("10000.00"), List.of(late));

        assertEquals(DelinquencyStatus.DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 2, 1)));
        assertTrue(loan.determineDelinquency(LocalDate.of(2026, 2, 1), policy));
    }

    @Test
    // T10 covers B3: evaluation date day before six-month anniversary (P3)
    void t10_dayBeforeSixMonthsStillNonDelinquent() {
        LoanPayment late = new LoanPayment(
                "p1", new BigDecimal("50.00"), BigDecimal.ZERO, LocalDate.of(2026, 1, 15), null);
        Loan loan = new Loan("L-1", new BigDecimal("10000.00"), List.of(late));
        LocalDate sixthMonthAnniversary = LocalDate.of(2026, 1, 15).plusMonths(6);

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, sixthMonthAnniversary.minusDays(1)));
    }

    @Test
    // T11 covers B4: evaluation date on six-month anniversary (P3)
    void t11_onSixMonthAnniversaryIsDelinquent() {
        LoanPayment late = new LoanPayment(
                "p1", new BigDecimal("50.00"), BigDecimal.ZERO, LocalDate.of(2026, 1, 15), null);
        Loan loan = new Loan("L-1", new BigDecimal("10000.00"), List.of(late));
        LocalDate sixthMonthAnniversary = LocalDate.of(2026, 1, 15).plusMonths(6);

        assertEquals(DelinquencyStatus.DELINQUENT, policy.evaluate(loan, sixthMonthAnniversary));
    }
}
