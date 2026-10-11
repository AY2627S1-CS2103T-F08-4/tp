package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final JsonAdaptedCardDetails VALID_CARD_DETAILS =
            new JsonAdaptedCardDetails(BENSON.getCardDetails());
    private static final String VALID_CREDIT_SCORE = BENSON.getCreditScore().toString();
    private static final String VALID_DEBT = BENSON.getDebt().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS,
                        VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS,
                        VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullCardDetails_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        String message = String.format(MISSING_FIELD_MESSAGE_FORMAT, CardDetails.class.getSimpleName());
        assertThrows(IllegalValueException.class, message, person::toModelType);
    }

    @Test
    public void toModelType_blankNestedCardField_throwsIllegalValueException() {
        JsonAdaptedCardDetails invalidCard = new JsonAdaptedCardDetails("4111111111111111", " ", "12/28", "Visa");
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                invalidCard, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        assertThrows(IllegalValueException.class, CardDetails.CVV_MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidExpiryDate_throwsIllegalValueException() {
        JsonAdaptedCardDetails invalidCard = new JsonAdaptedCardDetails("4111111111111111", "123", "13/28", "Visa");
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                invalidCard, VALID_CREDIT_SCORE, VALID_DEBT, VALID_TAGS);
        assertThrows(IllegalValueException.class, CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidCreditScore_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, "-1", VALID_DEBT, VALID_TAGS);
        assertThrows(IllegalValueException.class, CreditScore.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullCreditScore_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, null, VALID_DEBT, VALID_TAGS);
        String message = String.format(MISSING_FIELD_MESSAGE_FORMAT, CreditScore.class.getSimpleName());
        assertThrows(IllegalValueException.class, message, person::toModelType);
    }

    @Test
    public void toModelType_invalidDebt_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, "1.234", VALID_TAGS);
        assertThrows(IllegalValueException.class, Debt.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullDebt_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_CARD_DETAILS, VALID_CREDIT_SCORE, null, VALID_TAGS);
        String message = String.format(MISSING_FIELD_MESSAGE_FORMAT, Debt.class.getSimpleName());
        assertThrows(IllegalValueException.class, message, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_CARD_DETAILS, VALID_CREDIT_SCORE, VALID_DEBT, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
