package seedu.address.model.person;

import java.util.Objects;

/**
 * Describes a value that is shared by a proposed client and an existing client.
 *
 * <p>A clash is deliberately represented as a separate value object so that
 * the command layer can decide whether the clash is fatal (reference number)
 * or whether it should ask the user for confirmation (phone, card number, or
 * email address).
 */
public record ClientClash(Field field, String value, Person existingClient) {

    /** The client field that was found to be duplicated. */
    public enum Field {
        REFERENCE,
        PHONE,
        CARD_NUMBER,
        EMAIL
    }

    /** Creates a validated clash description. */
    public ClientClash {
        Objects.requireNonNull(field);
        Objects.requireNonNull(value);
        Objects.requireNonNull(existingClient);
    }

    /** Returns true when this clash concerns a client reference. */
    public boolean isReferenceClash() {
        return field == Field.REFERENCE;
    }

    /** Returns a message suitable for displaying to the user. */
    public String formatMessage() {
        String displayValue = field == Field.CARD_NUMBER
                ? maskCardNumber(value)
                : value;
        return field + " '" + displayValue + "' clashes with client "
                + existingClient.getClientReference()
                + " (" + existingClient.getName() + ")";
    }

    /** Masks all but the last four characters of a card number for display. */
    private static String maskCardNumber(String cardNumber) {
        return cardNumber.length() <= 4
                ? cardNumber
                : "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
