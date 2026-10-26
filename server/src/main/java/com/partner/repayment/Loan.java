package com.partner.repayment;

import java.math.BigDecimal;

/** A borrower's loan. Creates repayment plans and enforces one active plan at a time (I3). */
public class Loan {
    private final String loanId;
    private final Borrower borrower;
    private final BigDecimal balance;
    private RepaymentPlan activePlan;

    /**
     * @throws IllegalArgumentException if the balance is not greater than zero (fixes D3)
     */
    public Loan(String loanId, Borrower borrower, BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan balance must be greater than zero");
        }
        this.loanId = loanId;
        this.borrower = borrower;
        this.balance = balance;
    }

    /**
     * Creates a repayment plan for this loan (R1).
     *
     * @throws IllegalArgumentException if the term is outside 1-60 months
     * @throws IllegalStateException    if the loan already has an active plan (I3)
     */
    public RepaymentPlan createRepaymentPlan(int termMonths) {
        if (termMonths < RepaymentPlan.MIN_TERM_MONTHS) {
            throw new IllegalArgumentException("Term must be at least " + RepaymentPlan.MIN_TERM_MONTHS + " month");
        }
        if (termMonths > RepaymentPlan.MAX_TERM_MONTHS) {
            throw new IllegalArgumentException("Term must be at most " + RepaymentPlan.MAX_TERM_MONTHS + " months");
        }
        if (activePlan != null) {
            throw new IllegalStateException("Loan already has an active repayment plan");
        }
        RepaymentPlan plan = new RepaymentPlan(this, termMonths);
        activePlan = plan;
        return plan;
    }

    public String getLoanId() { return loanId; }
    public Borrower getBorrower() { return borrower; }
    public BigDecimal getBalance() { return balance; }
    public RepaymentPlan getActivePlan() { return activePlan; }
}
