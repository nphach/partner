package com.partner.web;

import com.partner.data.LoanDatabase;
import com.partner.domain.DelinquencyPolicy;
import com.partner.domain.DelinquencyStatus;
import com.partner.domain.Loan;
import java.time.LocalDate;
import java.util.Objects;

public class DelinquencyController {

    private final LoanDatabase loanDatabase;
    private final DelinquencyPolicy delinquencyPolicy;

    public DelinquencyController(LoanDatabase loanDatabase, DelinquencyPolicy delinquencyPolicy) {
        this.loanDatabase = Objects.requireNonNull(loanDatabase);
        this.delinquencyPolicy = Objects.requireNonNull(delinquencyPolicy);
    }

    public DelinquencyStatus checkDelinquency(String loanId, LocalDate currentDate) {
        Loan loan = loanDatabase
                .findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown loan: " + loanId));
        DelinquencyStatus status = loan.syncDelinquency(currentDate, delinquencyPolicy);
        loanDatabase.save(loan);
        return status;
    }
}
