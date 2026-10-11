package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;

/** A nonnegative whole-number credit score. */
public final class CreditScore {
    public static final String MESSAGE_CONSTRAINTS = "Credit score must be a nonnegative whole number.";

    private final BigInteger value;

    /** Creates a credit score from its decimal representation. */
    public CreditScore(String value) {
        requireNonNull(value);
        if (!isValidCreditScore(value)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.value = new BigInteger(value);
    }

    public static boolean isValidCreditScore(String value) {
        return value != null && value.matches("[0-9]+");
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof CreditScore score && value.equals(score.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
