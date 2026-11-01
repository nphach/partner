package com.partner.domain;

import java.util.Objects;

public class Borrower {

    private final String borrowerId;
    private final String name;

    public Borrower(String borrowerId, String name) {
        this.borrowerId = Objects.requireNonNull(borrowerId);
        this.name = Objects.requireNonNull(name);
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public String getName() {
        return name;
    }
}
