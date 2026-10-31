package com.partner.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class LoanPayment {

    private final String paymentId;
    private final BigDecimal amountDue;
    private BigDecimal amountPaid;
    private final LocalDate dueDate;
    private LocalDate paymentDate;
    private PaymentStatus status;

    public LoanPayment(
            String paymentId,
            BigDecimal amountDue,
            BigDecimal amountPaid,
            LocalDate dueDate,
            LocalDate paymentDate) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.amountDue = Objects.requireNonNull(amountDue);
        this.amountPaid = Objects.requireNonNull(amountPaid);
        this.dueDate = Objects.requireNonNull(dueDate);
        this.paymentDate = paymentDate;
        this.status = PaymentStatus.PENDING;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void recordPayment(BigDecimal amount, LocalDate paidOn) {
        amountPaid = amountPaid.add(amount);
        paymentDate = paidOn;
    }

    /**
     * Evaluates this installment as of {@code currentDate} and stores the result on {@link #status}.
     */
    public PaymentStatus checkPaymentStatus(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        if (amountPaid.compareTo(amountDue) >= 0) {
            status = PaymentStatus.PAID;
            return status;
        }
        if (!currentDate.isAfter(dueDate)) {
            status = PaymentStatus.PENDING;
            return status;
        }
        if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            status = PaymentStatus.PARTIALLY_PAID_LATE;
            return status;
        }
        status = PaymentStatus.MISSED;
        return status;
    }

    public long calculateDaysPastDue(LocalDate currentDate) {
        Objects.requireNonNull(currentDate);
        if (amountPaid.compareTo(amountDue) >= 0) {
            return 0L;
        }
        if (!currentDate.isAfter(dueDate)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(dueDate, currentDate);
    }

    public BigDecimal unpaidBalance() {
        return amountDue.subtract(amountPaid).max(BigDecimal.ZERO);
    }

    public boolean isPastDue(LocalDate currentDate) {
        return checkPaymentStatus(currentDate) == PaymentStatus.MISSED
                || checkPaymentStatus(currentDate) == PaymentStatus.PARTIALLY_PAID_LATE;
    }
}
