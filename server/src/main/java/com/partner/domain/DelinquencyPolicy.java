package com.partner.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Placeholder delinquency rules until institution-specific regulatory criteria are defined (CP2).
 * A loan is delinquent when either threshold is met.
 */
public class DelinquencyPolicy {

    public static final int PAST_DUE_MONTHS_THRESHOLD = 6;
    public static final BigDecimal UNPAID_INITIAL_FRACTION_THRESHOLD = new BigDecimal("0.25");

    /**
     * @return {@link DelinquencyStatus#DELINQUENT} when six-or-more-calendar-months past due on any
     *         late installment, or when unpaid missed balances exceed 25% of initial loan amount
     */
    public DelinquencyStatus evaluate(Loan loan, LocalDate currentDate) {
        if (loan.hasInstallmentAtLeastSixMonthsPastDue(currentDate)
                || exceedsUnpaidFractionThreshold(loan, currentDate)) {
            return DelinquencyStatus.DELINQUENT;
        }
        return DelinquencyStatus.NON_DELINQUENT;
    }

    private boolean exceedsUnpaidFractionThreshold(Loan loan, LocalDate currentDate) {
        BigDecimal threshold =
                loan.getInitialLoanAmount().multiply(UNPAID_INITIAL_FRACTION_THRESHOLD);
        return loan.calculateAmountPastDue(currentDate).compareTo(threshold) > 0;
    }
}
