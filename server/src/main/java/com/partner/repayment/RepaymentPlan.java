package com.partner.repayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** A plan to repay a loan's balance in equal monthly amounts. */
public class RepaymentPlan {
    public static final int MIN_TERM_MONTHS = 1;
    public static final int MAX_TERM_MONTHS = 60;

    private final Loan loan;
    private final int termMonths;
    private final BigDecimal newMonthlyAmount;
    private PlanStatus status;
    private IncentiveOffer incentiveOffer;

    RepaymentPlan(Loan loan, int termMonths) {
        this.loan = loan;
        this.termMonths = termMonths;
        this.newMonthlyAmount = calculateNewMonthlyAmount(termMonths);
        this.status = PlanStatus.PENDING;
    }

    /**
     * Balance divided by the term, rounded UP to the cent, so the plan never
     * collects less than the balance (I4).
     */
    public BigDecimal calculateNewMonthlyAmount(int termMonths) {
        return loan.getBalance().divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.CEILING);
    }

    /**
     * Creates a temporary reduced-payment offer if the borrower is eligible (R2).
     *
     * @throws IllegalStateException if the borrower is not eligible
     */
    public IncentiveOffer generateIncentiveOffer(LocalDate createdOn) {
        if (!IncentiveOffer.checkEligibility(loan.getBorrower(), this)) {
            throw new IllegalStateException("Borrower is not eligible for an incentive offer");
        }
        incentiveOffer = new IncentiveOffer(this, createdOn);
        return incentiveOffer;
    }

    /**
     * Approves the plan only if it passes regulatory validation (R4, I1).
     *
     * @return true if approved, false if validation failed (status stays PENDING)
     * @throws IllegalStateException if the plan is not PENDING
     */
    public boolean approve(RegulatoryValidator validator) {
        if (status != PlanStatus.PENDING) {
            throw new IllegalStateException("Only a pending plan can be approved");
        }
        if (!validator.validate(this)) {
            return false;
        }
        status = PlanStatus.APPROVED;
        return true;
    }

    public Loan getLoan() { return loan; }
    public int getTermMonths() { return termMonths; }
    public BigDecimal getNewMonthlyAmount() { return newMonthlyAmount; }
    public PlanStatus getStatus() { return status; }
    public IncentiveOffer getIncentiveOffer() { return incentiveOffer; }
}
