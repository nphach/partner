package com.partner.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Loan {

    private final String loanId;
    /** Original principal at disbursement; used for the unpaid-fraction delinquency rule. */
    private final BigDecimal initialLoanAmount;
    private final List<LoanPayment> payments;
    private Delinquency activeDelinquency;

    public Loan(String loanId, BigDecimal initialLoanAmount, List<LoanPayment> payments) {
        this.loanId = Objects.requireNonNull(loanId);
        this.initialLoanAmount = Objects.requireNonNull(initialLoanAmount);
        this.payments = new ArrayList<>(Objects.requireNonNull(payments));
        this.activeDelinquency = null;
    }

    public String getLoanId() {
        return loanId;
    }

    public BigDecimal getInitialLoanAmount() {
        return initialLoanAmount;
    }

    public List<LoanPayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public Delinquency getActiveDelinquency() {
        return activeDelinquency;
    }

    public List<LoanPayment> checkMissedPayments(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        List<LoanPayment> missed = new ArrayList<>();
        for (LoanPayment payment : payments) {
            PaymentStatus status = payment.checkPaymentStatus(currentDate);
            if (status == PaymentStatus.MISSED || status == PaymentStatus.PARTIALLY_PAID_LATE) {
                missed.add(payment);
            }
        }
        return missed;
    }

    public BigDecimal calculateAmountPastDue(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        BigDecimal total = BigDecimal.ZERO;
        for (LoanPayment payment : payments) {
            PaymentStatus status = payment.checkPaymentStatus(currentDate);
            if (status == PaymentStatus.MISSED || status == PaymentStatus.PARTIALLY_PAID_LATE) {
                total = total.add(payment.unpaidBalance());
            }
        }
        return total;
    }

    public long calculateMaxDaysPastDue(LocalDate currentDate) {
        long max = 0L;
        for (LoanPayment payment : payments) {
            max = Math.max(max, payment.calculateDaysPastDue(currentDate));
        }
        return max;
    }

    public boolean hasInstallmentAtLeastSixMonthsPastDue(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        for (LoanPayment payment : payments) {
            PaymentStatus status = payment.checkPaymentStatus(currentDate);
            if (status == PaymentStatus.MISSED || status == PaymentStatus.PARTIALLY_PAID_LATE) {
                if (!currentDate.isBefore(payment.getDueDate().plusMonths(DelinquencyPolicy.PAST_DUE_MONTHS_THRESHOLD))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean determineDelinquency(LocalDate currentDate, DelinquencyPolicy policy) {
        return policy.evaluate(this, currentDate) == DelinquencyStatus.DELINQUENT;
    }

    /**
     * Applies {@link DelinquencyPolicy} to payment history and updates the open {@link Delinquency} record.
     */
    public DelinquencyStatus syncDelinquency(LocalDate currentDate, DelinquencyPolicy policy) {
        Objects.requireNonNull(currentDate);
        Objects.requireNonNull(policy);
        long maxDays = calculateMaxDaysPastDue(currentDate);
        BigDecimal amountPastDue = calculateAmountPastDue(currentDate);
        DelinquencyStatus newStatus = policy.evaluate(this, currentDate);

        if (newStatus == DelinquencyStatus.NON_DELINQUENT) {
            if (activeDelinquency != null && activeDelinquency.isOpen()) {
                activeDelinquency.resolve(currentDate);
            }
            return DelinquencyStatus.NON_DELINQUENT;
        }

        if (activeDelinquency == null || !activeDelinquency.isOpen()) {
            activeDelinquency = Delinquency.open(currentDate, newStatus, maxDays, amountPastDue);
        } else {
            activeDelinquency.updateDelinquencyStatus(newStatus, maxDays, amountPastDue, currentDate);
        }
        return newStatus;
    }
}
