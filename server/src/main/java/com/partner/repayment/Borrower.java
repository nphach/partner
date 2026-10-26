package com.partner.repayment;

/**
 * Minimal borrower for the repayment slice. Delinquency detection belongs to
 * the delinquency slice; here it is just a flag the incentive rule reads.
 */
public class Borrower {
    private final String borrowerId;
    private final String name;
    private final boolean delinquent;

    public Borrower(String borrowerId, String name, boolean delinquent) {
        this.borrowerId = borrowerId;
        this.name = name;
        this.delinquent = delinquent;
    }

    public String getBorrowerId() { return borrowerId; }
    public String getName() { return name; }
    public boolean isDelinquent() { return delinquent; }
}
