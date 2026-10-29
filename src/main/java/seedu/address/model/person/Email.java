package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS =
            "Email must be in the form local-part@domain, for example alex@example.com.";
    public static final String VALIDATION_REGEX =
            "[^\\s\\p{Z}@]+@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+"
                    + "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(VALIDATION_REGEX)
                && test.substring(test.lastIndexOf('.') + 1).replaceAll("[^a-zA-Z]", "").length() >= 2;
    }

    /** Compares mailbox identities, preserving local-part case and ignoring domain case. */
    public boolean isSameMailboxAs(Email other) {
        requireNonNull(other);
        int separator = value.indexOf('@');
        int otherSeparator = other.value.indexOf('@');
        return value.substring(0, separator).equals(other.value.substring(0, otherSeparator))
                && value.substring(separator + 1).equalsIgnoreCase(other.value.substring(otherSeparator + 1));
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
