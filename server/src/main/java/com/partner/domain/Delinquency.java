package com.partner.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Delinquency {

    private final String delinquencyId;
    private DelinquencyStatus status;
    private LocalDate startDate;
    private LocalDate resolvedDate;
    private long daysPastDue;
    private BigDecimal amountPastDue;

    private Delinquency(
            String delinquencyId,
            DelinquencyStatus status,
            LocalDate startDate,
            long daysPastDue,
            BigDecimal amountPastDue) {
        this.delinquencyId = delinquencyId;
        this.status = status;
        this.startDate = startDate;
        this.daysPastDue = daysPastDue;
        this.amountPastDue = amountPastDue;
    }

    public static Delinquency open(
            LocalDate asOf, DelinquencyStatus status, long daysPastDue, BigDecimal amountPastDue) {
        return new Delinquency(UUID.randomUUID().toString(), status, asOf, daysPastDue, amountPastDue);
    }

    public void updateDelinquencyStatus(
            DelinquencyStatus newStatus, long daysPastDue, BigDecimal amountPastDue, LocalDate asOf) {
        Objects.requireNonNull(newStatus);
        this.status = newStatus;
        this.daysPastDue = daysPastDue;
        this.amountPastDue = amountPastDue;
        if (newStatus == DelinquencyStatus.NON_DELINQUENT) {
            resolve(asOf);
        } else if (resolvedDate != null) {
            resolvedDate = null;
        }
    }

    public void resolve(LocalDate asOf) {
        status = DelinquencyStatus.NON_DELINQUENT;
        resolvedDate = asOf;
    }

    public boolean isOpen() {
        return resolvedDate == null && status != DelinquencyStatus.NON_DELINQUENT;
    }

    public String getDelinquencyId() {
        return delinquencyId;
    }

    public DelinquencyStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getResolvedDate() {
        return resolvedDate;
    }

    public long getDaysPastDue() {
        return daysPastDue;
    }

    public BigDecimal getAmountPastDue() {
        return amountPastDue;
    }
}
