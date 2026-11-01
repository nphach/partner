package com.partner.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoanSyncDelinquencyBranchTest {

    private final DelinquencyPolicy policy = new DelinquencyPolicy();

    private static Loan loanWithMissed(BigDecimal initial, BigDecimal installmentDue, LocalDate dueDate) {
        LoanPayment missed = new LoanPayment("p1", installmentDue, BigDecimal.ZERO, dueDate, null);
        return new Loan("L-sync", initial, List.of(missed));
    }

    @Test
    // covers Path 1: N1 -> N2(yes) -> N3(no) -> N6 — missed payment but below delinquency thresholds
    void missedInstallmentButStillCurrent() {
        Loan loan = loanWithMissed(new BigDecimal("10000.00"), new BigDecimal("500.00"), LocalDate.of(2026, 1, 1));
        loan.getPayments().get(0).checkPaymentStatus(LocalDate.of(2026, 6, 15));

        DelinquencyStatus result = loan.syncDelinquency(LocalDate.of(2026, 6, 15), policy);

        assertEquals(DelinquencyStatus.NON_DELINQUENT, result);
        assertNull(loan.getActiveDelinquency());
    }

    @Test
    // covers Path 2: N1 -> N2(yes) -> N3(yes) -> N4 -> N6 — delinquent, then cure
    void cureClosesOpenDelinquency() {
        Loan loan = loanWithMissed(new BigDecimal("10000.00"), new BigDecimal("2600.00"), LocalDate.of(2026, 1, 1));
        loan.syncDelinquency(LocalDate.of(2026, 2, 1), policy);

        loan.getPayments().get(0).recordPayment(new BigDecimal("2600.00"), LocalDate.of(2026, 2, 5));
        DelinquencyStatus result = loan.syncDelinquency(LocalDate.of(2026, 2, 6), policy);

        assertEquals(DelinquencyStatus.NON_DELINQUENT, result);
        assertEquals(DelinquencyStatus.NON_DELINQUENT, loan.getActiveDelinquency().getStatus());
        assertEquals(LocalDate.of(2026, 2, 6), loan.getActiveDelinquency().getResolvedDate());
    }

    @Test
    // covers Path 3: N1 -> N2(no) -> N7(yes) -> N8 -> N10 — first crossing into delinquent (>25% unpaid)
    void firstDelinquencyOpensRecord() {
        Loan loan = loanWithMissed(new BigDecimal("10000.00"), new BigDecimal("2500.01"), LocalDate.of(2026, 1, 1));

        DelinquencyStatus result = loan.syncDelinquency(LocalDate.of(2026, 2, 1), policy);

        assertEquals(DelinquencyStatus.DELINQUENT, result);
        assertTrue(loan.getActiveDelinquency().isOpen());
        assertEquals(31L, loan.getActiveDelinquency().getDaysPastDue());
        assertEquals(new BigDecimal("2500.01"), loan.getActiveDelinquency().getAmountPastDue());
    }

    @Test
    // covers Path 4: N1 -> N2(no) -> N7(no) -> N9 -> N10 — still delinquent, metrics update
    void worseningPastDueUpdatesExistingRecord() {
        Loan loan = loanWithMissed(new BigDecimal("10000.00"), new BigDecimal("2500.01"), LocalDate.of(2026, 1, 1));
        loan.syncDelinquency(LocalDate.of(2026, 2, 1), policy);
        var first = loan.getActiveDelinquency();

        DelinquencyStatus result = loan.syncDelinquency(LocalDate.of(2026, 3, 15), policy);

        assertEquals(DelinquencyStatus.DELINQUENT, result);
        assertSame(first, loan.getActiveDelinquency());
        assertEquals(73L, loan.getActiveDelinquency().getDaysPastDue());
        assertEquals(new BigDecimal("2500.01"), loan.getActiveDelinquency().getAmountPastDue());
    }
}
