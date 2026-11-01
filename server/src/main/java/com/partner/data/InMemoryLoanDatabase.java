package com.partner.data;

import com.partner.domain.Loan;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryLoanDatabase implements LoanDatabase {

    private final Map<String, Loan> loans = new HashMap<>();

    @Override
    public Optional<Loan> findById(String loanId) {
        return Optional.ofNullable(loans.get(loanId));
    }

    @Override
    public void save(Loan loan) {
        loans.put(loan.getLoanId(), loan);
    }
}
