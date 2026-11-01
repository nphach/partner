package com.partner.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.partner.data.InMemoryLoanDatabase;
import com.partner.domain.DelinquencyPolicy;
import com.partner.domain.DelinquencyStatus;
import com.partner.domain.Loan;
import com.partner.domain.LoanPayment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies test basis R5: load loan, synchronize delinquency, persist updated state.
 */
class DelinquencyControllerTest {

    @Test
    void checkDelinquency_syncsStateAndPersistsLoan() {
        LoanPayment missed = new LoanPayment(
                "p1", new BigDecimal("2600.00"), BigDecimal.ZERO, LocalDate.of(2026, 1, 1), null);
        Loan loan = new Loan("L-ctrl-1", new BigDecimal("10000.00"), List.of(missed));
        InMemoryLoanDatabase database = new InMemoryLoanDatabase();
        database.save(loan);
        DelinquencyController controller = new DelinquencyController(database, new DelinquencyPolicy());

        DelinquencyStatus status = controller.checkDelinquency("L-ctrl-1", LocalDate.of(2026, 2, 15));

        assertEquals(DelinquencyStatus.DELINQUENT, status);
        Loan persisted = database.findById("L-ctrl-1").orElseThrow();
        assertNotNull(persisted.getActiveDelinquency());
        assertEquals(DelinquencyStatus.DELINQUENT, persisted.getActiveDelinquency().getStatus());
        assertEquals(45L, persisted.getActiveDelinquency().getDaysPastDue());
    }

    @Test
    void checkDelinquency_unknownLoanId_rejected() {
        DelinquencyController controller =
                new DelinquencyController(new InMemoryLoanDatabase(), new DelinquencyPolicy());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.checkDelinquency("missing", LocalDate.of(2026, 1, 1)));
    }
}
