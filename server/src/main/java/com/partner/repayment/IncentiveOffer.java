package com.partner.repayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A temporary reduced-payment offer, modeled on lender hardship programs:
 * the borrower pays 75% of the monthly amount for the first 3 months, and the
 * shortfall is spread over the remaining months so the full balance is still
 * repaid (I4). Nothing is forgiven.
 */
public class IncentiveOffer {
    public static final int VALID_DAYS = 14;
    public static final BigDecimal REDUCED_RATE = new BigDecimal("0.75");
    public static final int REDUCED_MONTHS = 3;
    /** At least one catch-up month is needed after the reduced months. */
    public static final int MIN_PLAN_TERM_FOR_OFFER = REDUCED_MONTHS + 1;

    private final RepaymentPlan repaymentPlan;
    private final BigDecimal reducedPaymentAmount;
    private final BigDecimal catchUpPaymentAmount;
    private final LocalDate expirationDate;
    private OfferStatus status;

    IncentiveOffer(RepaymentPlan repaymentPlan, LocalDate createdOn) {
        this.repaymentPlan = repaymentPlan;

        // 75% of the normal monthly amount, rounded up to the cent
        this.reducedPaymentAmount = repaymentPlan.getNewMonthlyAmount()
                .multiply(REDUCED_RATE)
                .setScale(2, RoundingMode.CEILING);

        // whatever is left after the reduced months, split over the remaining months, rounded up
        BigDecimal paidDuringReduced = reducedPaymentAmount.multiply(BigDecimal.valueOf(REDUCED_MONTHS));
        BigDecimal remainingBalance = repaymentPlan.getLoan().getBalance().subtract(paidDuringReduced);
        int catchUpMonths = repaymentPlan.getTermMonths() - REDUCED_MONTHS;
        this.catchUpPaymentAmount = remainingBalance.divide(
                BigDecimal.valueOf(catchUpMonths), 2, RoundingMode.CEILING);

        this.expirationDate = createdOn.plusDays(VALID_DAYS);
        this.status = OfferStatus.PENDING;
    }

    /**
     * Eligible only if the borrower is delinquent and the plan is long enough
     * to have at least one catch-up month after the reduced months (R2).
     */
    public static boolean checkEligibility(Borrower borrower, RepaymentPlan plan) {
        return borrower.isDelinquent() && plan.getTermMonths() >= MIN_PLAN_TERM_FOR_OFFER;
    }

    /**
     * Accepts the offer (R3). Allowed on or before the expiration date (I2).
     *
     * @throws IllegalStateException if the offer is not pending, or is past expiration
     *                               (in which case it is marked EXPIRED)
     */
    public void accept(LocalDate today) {
        if (status != OfferStatus.PENDING) {
            throw new IllegalStateException("Offer is " + status + ", not pending");
        }
        if (today.isAfter(expirationDate)) {
            status = OfferStatus.EXPIRED;
            throw new IllegalStateException("Offer expired on " + expirationDate);
        }
        status = OfferStatus.ACCEPTED;
    }

    /**
     * Declines the offer (R3).
     *
     * @throws IllegalStateException if the offer is not pending, or is past expiration
     */
    public void decline(LocalDate today) {
        if (status != OfferStatus.PENDING) {
            throw new IllegalStateException("Offer is " + status + ", not pending");
        }
        if (today.isAfter(expirationDate)) {
            status = OfferStatus.EXPIRED;
            throw new IllegalStateException("Offer expired on " + expirationDate);
        }
        status = OfferStatus.DECLINED;
    }

    /** Expires a still-pending offer once its expiration date has passed (R5). */
    public void expire(LocalDate today) {
        if (status == OfferStatus.PENDING && today.isAfter(expirationDate)) {
            status = OfferStatus.EXPIRED;
        }
    }

    /** Total the borrower pays if they take the offer: reduced months plus catch-up months. */
    public BigDecimal getTotalScheduled() {
        int catchUpMonths = repaymentPlan.getTermMonths() - REDUCED_MONTHS;
        return reducedPaymentAmount.multiply(BigDecimal.valueOf(REDUCED_MONTHS))
                .add(catchUpPaymentAmount.multiply(BigDecimal.valueOf(catchUpMonths)));
    }

    public RepaymentPlan getRepaymentPlan() { return repaymentPlan; }
    public BigDecimal getReducedPaymentAmount() { return reducedPaymentAmount; }
    public BigDecimal getCatchUpPaymentAmount() { return catchUpPaymentAmount; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public OfferStatus getStatus() { return status; }
}
