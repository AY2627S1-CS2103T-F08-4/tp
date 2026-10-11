package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.CardDetails;

/** Jackson-friendly version of {@link CardDetails}. */
class JsonAdaptedCardDetails {
    private final String cardNumber;
    private final String cvv;
    private final String expiryDate;
    private final String provider;

    /** Creates card details from JSON fields. */
    @JsonCreator
    public JsonAdaptedCardDetails(@JsonProperty("cardNumber") String cardNumber,
            @JsonProperty("cvv") String cvv, @JsonProperty("expiryDate") String expiryDate,
            @JsonProperty("provider") String provider) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.provider = provider;
    }

    /** Creates the JSON form of a person's card details. */
    public JsonAdaptedCardDetails(CardDetails source) {
        cardNumber = source.getCardNumber();
        cvv = source.getCvv();
        expiryDate = source.getExpiryDate();
        provider = source.getProvider();
    }

    /** Converts the JSON form into card details. */
    public CardDetails toModelType() throws IllegalValueException {
        if (cardNumber == null) {
            throw new IllegalValueException(CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS);
        }
        if (cvv == null) {
            throw new IllegalValueException(CardDetails.CVV_MESSAGE_CONSTRAINTS);
        }
        if (expiryDate == null) {
            throw new IllegalValueException(CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS);
        }
        if (provider == null) {
            throw new IllegalValueException(CardDetails.PROVIDER_MESSAGE_CONSTRAINTS);
        }
        try {
            return new CardDetails(cardNumber, cvv, expiryDate, provider);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
