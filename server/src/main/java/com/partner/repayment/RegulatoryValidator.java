package com.partner.repayment;

/** Checks a plan against regulatory limits. Kept behind an interface because limits vary by state (ADR-002). */
public interface RegulatoryValidator {
    boolean validate(RepaymentPlan plan);
}
