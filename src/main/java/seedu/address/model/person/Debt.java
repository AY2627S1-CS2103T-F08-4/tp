package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;

/** A nonnegative amount with two decimal places. No currency is implied. */
public final class Debt {
    public static final String MESSAGE_CONSTRAINTS =
            "Debt must be a nonnegative amount with at most two decimal places.";

    private final BigDecimal amount;

    /** Creates a debt amount from its decimal representation. */
    public Debt(String value) {
        requireNonNull(value);
        if (!isValidDebt(value)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        amount = new BigDecimal(value).setScale(2);
    }

    public static boolean isValidDebt(String value) {
        return value != null && value.matches("[0-9]+(?:\\.[0-9]{1,2})?");
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Debt debt && amount.equals(debt.amount);
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }
}
