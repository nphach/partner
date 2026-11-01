package com.partner.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Branch coverage for {@link DelinquencyPolicy#evaluate(Loan, LocalDate)}.
 *
 * <p>Basis: NON_DELINQUENT unless (1) any late installment is at least 6 calendar months past due,
 * or (2) unpaid missed balances exceed 25% of initial loan amount.
 */
class DelinquencyPolicyEvaluateBranchTest {

    private final DelinquencyPolicy policy = new DelinquencyPolicy();

    private static Loan loanWithMissed(
            String loanId, BigDecimal initial, BigDecimal installmentDue, LocalDate dueDate) {
        LoanPayment missed = new LoanPayment("p1", installmentDue, BigDecimal.ZERO, dueDate, null);
        return new Loan(loanId, initial, List.of(missed));
    }

    @Test
    // Path 1: six-month rule false AND unpaid-fraction rule false -> NON_DELINQUENT
    void underBothThresholds() {
        Loan loan = loanWithMissed("L-p1", new BigDecimal("10000.00"), new BigDecimal("500.00"), LocalDate.of(2025, 8, 1));

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 1, 1)));
    }

    @Test
    // Path 2: six-month rule false AND unpaid-fraction rule true -> DELINQUENT
    void unpaidFractionExceedsTwentyFivePercent() {
        Loan loan = loanWithMissed(
                "L-p2", new BigDecimal("10000.00"), new BigDecimal("2500.01"), LocalDate.of(2026, 1, 1));

        assertEquals(DelinquencyStatus.DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 2, 1)));
    }

    @Test
    // Path 3: six-month rule true (short-circuit OR) -> DELINQUENT
    void sixCalendarMonthsPastDue() {
        Loan loan = loanWithMissed("L-p3", new BigDecimal("10000.00"), new BigDecimal("50.00"), LocalDate.of(2026, 1, 15));
        LocalDate onAnniversary = LocalDate.of(2026, 1, 15).plusMonths(6);

        assertEquals(DelinquencyStatus.DELINQUENT, policy.evaluate(loan, onAnniversary));
    }

    @Test
    // Boundary: unpaid fraction exactly 25% -> NON_DELINQUENT (rule is strictly greater than 25%)
    void boundary_exactlyTwentyFivePercentUnpaid() {
        Loan loan = loanWithMissed(
                "L-b1", new BigDecimal("10000.00"), new BigDecimal("2500.00"), LocalDate.of(2026, 1, 1));

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 2, 1)));
    }

    @Test
    // Boundary: day before six-month anniversary, small unpaid (time rule false, fraction false)
    void boundary_dayBeforeSixMonths() {
        Loan loan = loanWithMissed("L-b2", new BigDecimal("10000.00"), new BigDecimal("50.00"), LocalDate.of(2026, 1, 15));
        LocalDate dayBefore = LocalDate.of(2026, 1, 15).plusMonths(6).minusDays(1);

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, dayBefore));
    }

    @Test
    // No late installments: both rules false -> NON_DELINQUENT
    void allInstallmentsPaid() {
        LoanPayment paid = new LoanPayment(
                "p1", new BigDecimal("400.00"), new BigDecimal("400.00"), LocalDate.of(2026, 1, 1), null);
        Loan loan = new Loan("L-paid", new BigDecimal("10000.00"), List.of(paid));

        assertEquals(DelinquencyStatus.NON_DELINQUENT, policy.evaluate(loan, LocalDate.of(2026, 8, 1)));
    }
}
