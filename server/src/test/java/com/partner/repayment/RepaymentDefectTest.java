package com.partner.repayment;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 4: tests tied to the defect log.
 * D2 and D3 were fixed; these tests prove the fixes and guard against regressions.
 * D1 is open (handed to CP2); its test is disabled so the build stays green,
 * and states the expected behavior once the team decides how the cap applies.
 */
class RepaymentDefectTest {

    private static Borrower delinquentBorrower() {
        return new Borrower("B-1", "Test Borrower", true);
    }

    // ---------- D1 (OPEN): catch-up payment can exceed the $5,000 cap ----------
    @Test
    @Disabled("D1 open for CP2: validator checks only the normal monthly amount, not the incentive's catch-up payment")
    void d1_catchUpPaymentOverCap_shouldBlockApproval() {
        // $15,000 over 4 months = $3,750.00/month (under the cap)
        RepaymentPlan plan = new Loan("L-1", delinquentBorrower(), new BigDecimal("15000.00"))
                .createRepaymentPlan(4);
        IncentiveOffer offer = plan.generateIncentiveOffer(LocalDate.of(2026, 11, 1));
        // reduced: 75% of 3,750.00 = 2,812.50 x 3 = 8,437.50; catch-up: 15,000 - 8,437.50 = 6,562.50
        assertEquals(new BigDecimal("6562.50"), offer.getCatchUpPaymentAmount());
        // Expected once fixed: a schedule with any payment over $5,000.00 is not approved (R4)
        assertFalse(plan.approve(new StandardRegulatoryValidator()));
        assertEquals(PlanStatus.PENDING, plan.getStatus());
    }

    // ---------- D2 (FIXED): second offer silently replaced an accepted one ----------
    @Test
    void d2_secondOffer_rejectedAndAcceptedOfferKept() {
        RepaymentPlan plan = new Loan("L-1", delinquentBorrower(), new BigDecimal("1000.00"))
                .createRepaymentPlan(10);
        IncentiveOffer first = plan.generateIncentiveOffer(LocalDate.of(2026, 11, 1));
        first.accept(LocalDate.of(2026, 11, 3));

        assertThrows(IllegalStateException.class,
                () -> plan.generateIncentiveOffer(LocalDate.of(2026, 11, 5)));
        assertSame(first, plan.getIncentiveOffer());
        assertEquals(OfferStatus.ACCEPTED, plan.getIncentiveOffer().getStatus());
    }

    // ---------- D3 (FIXED): zero or negative balance was accepted ----------
    @Test // below the boundary
    void d3_negativeBalance_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("L-1", delinquentBorrower(), new BigDecimal("-0.01")));
    }

    @Test // at the boundary
    void d3_zeroBalance_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("L-1", delinquentBorrower(), new BigDecimal("0.00")));
    }

    @Test // just above the boundary: one cent is a valid balance
    void d3_oneCentBalance_accepted() {
        Loan loan = new Loan("L-1", delinquentBorrower(), new BigDecimal("0.01"));
        RepaymentPlan plan = loan.createRepaymentPlan(1);
        assertEquals(new BigDecimal("0.01"), plan.getNewMonthlyAmount());
        assertSame(plan, loan.getActivePlan());
    }
}
