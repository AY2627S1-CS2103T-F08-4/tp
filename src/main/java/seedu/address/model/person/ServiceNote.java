package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

/** A single, short service note stored on a client profile. */
public final class ServiceNote {
    public static final String MESSAGE_CONSTRAINTS =
            "Service note must contain at most 200 printable characters on one line.";
    private static final String VALIDATION_REGEX = "[\\p{L}\\p{M}\\p{N}\\p{P}\\p{S}\\p{Zs}]{1,200}";

    public final String value;

    /** Creates a validated, canonical note value. */
    public ServiceNote(String value) {
        requireNonNull(value);
        checkArgument(isValidServiceNote(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    /** A stored note is nonblank, printable, and already trimmed. */
    public static boolean isValidServiceNote(String value) {
        return value != null && value.equals(value.strip()) && value.matches(VALIDATION_REGEX)
                && !value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ServiceNote note && value.equals(note.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
