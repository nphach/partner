package com.partner.data;

import com.partner.domain.Loan;
import java.util.Optional;

public interface LoanDatabase {

    Optional<Loan> findById(String loanId);

    void save(Loan loan);
}
