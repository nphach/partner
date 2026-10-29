package com.partner.repayment;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 2: black-box tests. Every expected value comes from the test basis
 * (R1-R5, I1-I4), worked out by hand before the code was run.
 */
class RepaymentBlackBoxTest {

    private static Loan loanWithBalance(String balance) {
        Borrower borrower = new Borrower("B-1", "Test Borrower", true);
        return new Loan("L-1", borrower, new BigDecimal(balance));
    }

    // ---------- Behavior 1: create a repayment plan (R1, I3, I4) ----------
    @Nested
    class CreatePlan {
        Loan loan;

        @BeforeEach
        void setUp() {
            loan = loanWithBalance("1000.00");
        }

        @Test // T1 covers P1
        void t1_term4_dividesEvenly() {
            RepaymentPlan plan = loan.createRepaymentPlan(4);
            assertEquals(new BigDecimal("250.00"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T2 covers P2: 1000/3 = 333.333... rounds UP to 333.34 (I4)
        void t2_term3_roundsUp() {
            RepaymentPlan plan = loan.createRepaymentPlan(3);
            assertEquals(new BigDecimal("333.34"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T3 covers P3 / B1
        void t3_term0_rejected() {
            assertThrows(IllegalArgumentException.class, () -> loan.createRepaymentPlan(0));
            assertNull(loan.getActivePlan());
        }

        @Test // T4 covers P3
        void t4_termNegative_rejected() {
            assertThrows(IllegalArgumentException.class, () -> loan.createRepaymentPlan(-1));
            assertNull(loan.getActivePlan());
        }

        @Test // T5 covers P1 / B2
        void t5_term1_lowerBoundary() {
            RepaymentPlan plan = loan.createRepaymentPlan(1);
            assertEquals(new BigDecimal("1000.00"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T6 covers P1 / B3
        void t6_term2_justAboveLowerBoundary() {
            RepaymentPlan plan = loan.createRepaymentPlan(2);
            assertEquals(new BigDecimal("500.00"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T7 covers P2 / B4: 59 x 16.95 = 1000.05 >= 1000.00
        void t7_term59_justBelowUpperBoundary() {
            RepaymentPlan plan = loan.createRepaymentPlan(59);
            assertEquals(new BigDecimal("16.95"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T8 covers P2 / B5: 60 x 16.67 = 1000.20 >= 1000.00
        void t8_term60_upperBoundary() {
            RepaymentPlan plan = loan.createRepaymentPlan(60);
            assertEquals(new BigDecimal("16.67"), plan.getNewMonthlyAmount());
            assertSame(plan, loan.getActivePlan());
        }

        @Test // T9 covers P4 / B6
        void t9_term61_rejected() {
            assertThrows(IllegalArgumentException.class, () -> loan.createRepaymentPlan(61));
            assertNull(loan.getActivePlan());
        }

        @Test // T10 covers P5 (I3)
        void t10_secondPlan_rejected() {
            RepaymentPlan first = loan.createRepaymentPlan(6);
            assertThrows(IllegalStateException.class, () -> loan.createRepaymentPlan(12));
            assertSame(first, loan.getActivePlan());
            assertEquals(6, loan.getActivePlan().getTermMonths());
        }
    }

    // ---------- Behavior 2: incentive offer, generate and accept (R2, R3, R5, I2, I4) ----------
    @Nested
    class IncentiveOfferLifecycle {
        private final LocalDate created = LocalDate.of(2026, 11, 1); // expires 2026-11-15

        private RepaymentPlan planFor(boolean delinquent, int termMonths) {
            Borrower borrower = new Borrower("B-1", "Test Borrower", delinquent);
            return new Loan("L-1", borrower, new BigDecimal("1000.00")).createRepaymentPlan(termMonths);
        }

        @Test // T11 covers P1: 3 x 75.00 + 7 x 110.72 = 1000.04 >= 1000.00 (I4)
        void t11_eligible_offerAmounts() {
            RepaymentPlan plan = planFor(true, 10);
            IncentiveOffer offer = plan.generateIncentiveOffer(created);
            assertEquals(new BigDecimal("75.00"), offer.getReducedPaymentAmount());
            assertEquals(new BigDecimal("110.72"), offer.getCatchUpPaymentAmount());
            assertEquals(new BigDecimal("1000.04"), offer.getTotalScheduled());
            assertEquals(LocalDate.of(2026, 11, 15), offer.getExpirationDate());
            assertEquals(OfferStatus.PENDING, offer.getStatus());
            assertSame(offer, plan.getIncentiveOffer());
        }

        @Test // T12 covers P2: borrower not delinquent
        void t12_notDelinquent_rejected() {
            RepaymentPlan plan = planFor(false, 10);
            assertThrows(IllegalStateException.class, () -> plan.generateIncentiveOffer(created));
            assertNull(plan.getIncentiveOffer());
        }

        @Test // T13 covers P3 / B1: 3-month plan leaves no catch-up month
        void t13_term3_rejected() {
            RepaymentPlan plan = planFor(true, 3);
            assertThrows(IllegalStateException.class, () -> plan.generateIncentiveOffer(created));
            assertNull(plan.getIncentiveOffer());
        }

        @Test // T14 covers P1 / B2: 3 x 187.50 + 1 x 437.50 = 1000.00
        void t14_term4_lowestEligible() {
            IncentiveOffer offer = planFor(true, 4).generateIncentiveOffer(created);
            assertEquals(new BigDecimal("187.50"), offer.getReducedPaymentAmount());
            assertEquals(new BigDecimal("437.50"), offer.getCatchUpPaymentAmount());
            assertEquals(new BigDecimal("1000.00"), offer.getTotalScheduled());
        }

        @Test // T15 covers P1 / B3: 3 x 150.00 + 2 x 275.00 = 1000.00
        void t15_term5_justAboveBoundary() {
            IncentiveOffer offer = planFor(true, 5).generateIncentiveOffer(created);
            assertEquals(new BigDecimal("150.00"), offer.getReducedPaymentAmount());
            assertEquals(new BigDecimal("275.00"), offer.getCatchUpPaymentAmount());
            assertEquals(new BigDecimal("1000.00"), offer.getTotalScheduled());
        }

        @Test // T16 covers P4
        void t16_acceptWellBeforeExpiration() {
            IncentiveOffer offer = planFor(true, 10).generateIncentiveOffer(created);
            offer.accept(LocalDate.of(2026, 11, 3));
            assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        }

        @Test // T17 covers P4 / B4
        void t17_acceptDayBeforeExpiration() {
            IncentiveOffer offer = planFor(true, 10).generateIncentiveOffer(created);
            offer.accept(LocalDate.of(2026, 11, 14));
            assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        }

        @Test // T18 covers P4 / B5: accepting ON the expiration date is allowed (I2)
        void t18_acceptOnExpirationDate() {
            IncentiveOffer offer = planFor(true, 10).generateIncentiveOffer(created);
            offer.accept(LocalDate.of(2026, 11, 15));
            assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        }

        @Test // T19 covers P5 / B6
        void t19_acceptDayAfterExpiration_rejected() {
            IncentiveOffer offer = planFor(true, 10).generateIncentiveOffer(created);
            assertThrows(IllegalStateException.class, () -> offer.accept(LocalDate.of(2026, 11, 16)));
            assertEquals(OfferStatus.EXPIRED, offer.getStatus());
        }

        @Test // T20 covers P6
        void t20_acceptAfterDecline_rejected() {
            IncentiveOffer offer = planFor(true, 10).generateIncentiveOffer(created);
            offer.decline(LocalDate.of(2026, 11, 2));
            assertThrows(IllegalStateException.class, () -> offer.accept(LocalDate.of(2026, 11, 3)));
            assertEquals(OfferStatus.DECLINED, offer.getStatus());
        }
    }

    // ---------- Behavior 3: approve a plan (R4, I1) ----------
    @Nested
    class ApprovePlan {
        RegulatoryValidator validator = new StandardRegulatoryValidator();

        @Test // T21 covers P1 / B1
        void t21_justBelowCap_approved() {
            RepaymentPlan plan = loanWithBalance("4999.99").createRepaymentPlan(1);
            assertTrue(plan.approve(validator));
            assertEquals(PlanStatus.APPROVED, plan.getStatus());
        }

        @Test // T22 covers P1 / B2
        void t22_atCap_approved() {
            RepaymentPlan plan = loanWithBalance("5000.00").createRepaymentPlan(1);
            assertTrue(plan.approve(validator));
            assertEquals(PlanStatus.APPROVED, plan.getStatus());
        }

        @Test // T23 covers P2 / B3
        void t23_justAboveCap_rejected() {
            RepaymentPlan plan = loanWithBalance("5000.01").createRepaymentPlan(1);
            assertFalse(plan.approve(validator));
            assertEquals(PlanStatus.PENDING, plan.getStatus());
        }

        @Test // T24 covers P2: 60000.01 / 12 rounds UP to 5000.01, over the cap
        void t24_roundingPushesOverCap_rejected() {
            RepaymentPlan plan = loanWithBalance("60000.01").createRepaymentPlan(12);
            assertEquals(new BigDecimal("5000.01"), plan.getNewMonthlyAmount());
            assertFalse(plan.approve(validator));
            assertEquals(PlanStatus.PENDING, plan.getStatus());
        }
    }
}
