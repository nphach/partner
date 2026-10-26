package com.partner.repayment;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 3: white-box tests for Loan.createRepaymentPlan(int termMonths).
 * One test per branch-coverage path through the control flow graph.
 * Each test checks the result (returned plan or thrown exception)
 * AND the loan's resulting state (its active plan).
 */
class RepaymentWhiteBoxTest {

    private Loan loan;

    @BeforeEach
    void setUp() {
        Borrower borrower = new Borrower("B-1", "Test Borrower", true);
        loan = new Loan("L-1", borrower, new BigDecimal("1200.00"));
    }

    @Test // covers Path 1: N1 -> N2 -> Exit   (D1 true: term below 1)
    void path1_termBelowMinimum_throwsAndCreatesNoPlan() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> loan.createRepaymentPlan(0));
        assertTrue(e.getMessage().contains("at least"));
        assertNull(loan.getActivePlan());
    }

    @Test // covers Path 2: N1 -> N3 -> N4 -> Exit   (D1 false, D2 true: term above 60)
    void path2_termAboveMaximum_throwsAndCreatesNoPlan() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> loan.createRepaymentPlan(61));
        assertTrue(e.getMessage().contains("at most"));
        assertNull(loan.getActivePlan());
    }

    @Test // covers Path 3: N1 -> N3 -> N5 -> N6 -> Exit   (D1 false, D2 false, D3 true: plan exists)
    void path3_loanAlreadyHasPlan_throwsAndKeepsExistingPlan() {
        RepaymentPlan existing = loan.createRepaymentPlan(6);
        assertThrows(IllegalStateException.class, () -> loan.createRepaymentPlan(12));
        assertSame(existing, loan.getActivePlan());
        assertEquals(6, loan.getActivePlan().getTermMonths());
        assertEquals(new BigDecimal("200.00"), loan.getActivePlan().getNewMonthlyAmount());
    }

    @Test // covers Path 4: N1 -> N3 -> N5 -> N7 -> Exit   (D1 false, D2 false, D3 false: plan created)
    void path4_validTermNoExistingPlan_returnsPlanAndSetsActivePlan() {
        RepaymentPlan plan = loan.createRepaymentPlan(12);
        assertNotNull(plan);
        assertEquals(12, plan.getTermMonths());
        assertEquals(new BigDecimal("100.00"), plan.getNewMonthlyAmount()); // 1200.00 / 12
        assertEquals(PlanStatus.PENDING, plan.getStatus());
        assertSame(loan, plan.getLoan());
        assertSame(plan, loan.getActivePlan());
    }
}
