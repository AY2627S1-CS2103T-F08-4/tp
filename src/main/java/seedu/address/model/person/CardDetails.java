package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/** The card details stored in a person's profile. */
public final class CardDetails {
    public static final String CARD_NUMBER_MESSAGE_CONSTRAINTS = "Card number must not be blank.";
    public static final String CVV_MESSAGE_CONSTRAINTS = "CVV must not be blank.";
    public static final String EXPIRY_DATE_MESSAGE_CONSTRAINTS =
            "Expiry date must be in MM/YY format with a month from 01 to 12 (e.g. 12/28).";
    public static final String PROVIDER_MESSAGE_CONSTRAINTS = "Provider must not be blank.";

    private final String cardNumber;
    private final String cvv;
    private final String expiryDate;
    private final String provider;

    /** Creates card details with all four fields present. */
    public CardDetails(String cardNumber, String cvv, String expiryDate, String provider) {
        requireAllNonNull(cardNumber, cvv, expiryDate, provider);
        this.cardNumber = requireNonBlank(cardNumber, CARD_NUMBER_MESSAGE_CONSTRAINTS);
        this.cvv = requireNonBlank(cvv, CVV_MESSAGE_CONSTRAINTS);
        checkArgument(isValidExpiryDate(expiryDate), EXPIRY_DATE_MESSAGE_CONSTRAINTS);
        this.expiryDate = expiryDate;
        this.provider = requireNonBlank(provider, PROVIDER_MESSAGE_CONSTRAINTS);
    }

    private static String requireNonBlank(String value, String message) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    /** Checks the MM/YY format and month range; past expiry dates are allowed. */
    public static boolean isValidExpiryDate(String value) {
        return value != null && value.matches("(0[1-9]|1[0-2])/[0-9]{2}");
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CardDetails otherCardDetails)) {
            return false;
        }
        return cardNumber.equals(otherCardDetails.cardNumber)
                && cvv.equals(otherCardDetails.cvv)
                && expiryDate.equals(otherCardDetails.expiryDate)
                && provider.equals(otherCardDetails.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, cvv, expiryDate, provider);
    }

}
