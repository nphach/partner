package com.partner.repayment;

import java.math.BigDecimal;

/** Default limits: term of at most 60 months and a monthly amount of at most $5,000.00 (R4). */
public class StandardRegulatoryValidator implements RegulatoryValidator {
    public static final BigDecimal DEFAULT_MAX_MONTHLY_AMOUNT = new BigDecimal("5000.00");
    public static final int DEFAULT_MAX_TERM_MONTHS = 60;

    private final BigDecimal maxMonthlyAmount;
    private final int maxTermMonths;

    public StandardRegulatoryValidator() {
        this(DEFAULT_MAX_MONTHLY_AMOUNT, DEFAULT_MAX_TERM_MONTHS);
    }

    public StandardRegulatoryValidator(BigDecimal maxMonthlyAmount, int maxTermMonths) {
        this.maxMonthlyAmount = maxMonthlyAmount;
        this.maxTermMonths = maxTermMonths;
    }

    @Override
    public boolean validate(RepaymentPlan plan) {
        return plan.getTermMonths() <= maxTermMonths
                && plan.getNewMonthlyAmount().compareTo(maxMonthlyAmount) <= 0;
    }
}
