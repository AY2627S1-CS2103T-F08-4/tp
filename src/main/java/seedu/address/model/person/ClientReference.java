package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * A client reference, normalized to uppercase for case-insensitive identity comparisons.
 */
public final class ClientReference {
    public static final String MESSAGE_CONSTRAINTS = "Client reference must start with a letter and contain only "
            + "letters, digits, or hyphens (maximum 20 characters).";
    public static final String VALIDATION_REGEX = "[a-zA-Z][a-zA-Z0-9-]{0,19}";

    public final String value;

    /**
     * Creates a validated, uppercase client reference.
     */
    public ClientReference(String reference) {
        requireNonNull(reference);
        String trimmed = reference.trim();
        checkArgument(isValidClientReference(trimmed), MESSAGE_CONSTRAINTS);
        value = trimmed.toUpperCase(Locale.ROOT);
    }

    /**
     * Returns whether the reference follows the US01 identifier rules.
     */
    public static boolean isValidClientReference(String reference) {
        return reference.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClientReference reference && value.equals(reference.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
