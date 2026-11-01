package com.partner.domain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Hunt for state-transition defects (Part 4). Exposes D1: {@link Loan} keeps a single
 * {@code activeDelinquency} pointer, so earlier resolved episodes are not reachable after re-default.
 */
class LoanDelinquencyHistoryTest {

    @Test
    void redefaultAfterCureReplacesPriorEpisodeReference() {
        LoanPayment firstInstallment = new LoanPayment(
                "p1", new BigDecimal("260.00"), BigDecimal.ZERO, LocalDate.of(2026, 1, 1), null);
        LoanPayment secondInstallment = new LoanPayment(
                "p2", new BigDecimal("260.00"), BigDecimal.ZERO, LocalDate.of(2026, 3, 1), null);
        Loan loan = new Loan("L-200", new BigDecimal("1000.00"), List.of(firstInstallment, secondInstallment));
        DelinquencyPolicy policy = new DelinquencyPolicy();

        loan.syncDelinquency(LocalDate.of(2026, 1, 20), policy);
        String firstEpisodeId = loan.getActiveDelinquency().getDelinquencyId();

        firstInstallment.recordPayment(new BigDecimal("260.00"), LocalDate.of(2026, 1, 25));
        loan.syncDelinquency(LocalDate.of(2026, 1, 26), policy);

        loan.syncDelinquency(LocalDate.of(2026, 3, 15), policy);
        String secondEpisodeId = loan.getActiveDelinquency().getDelinquencyId();

        assertNotEquals(firstEpisodeId, secondEpisodeId);
    }
}
