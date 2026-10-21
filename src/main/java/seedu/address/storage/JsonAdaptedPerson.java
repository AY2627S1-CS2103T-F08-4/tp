package seedu.address.storage;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String INVALID_TIMESTAMP_MESSAGE = "Timestamp information is invalid for this profile.";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String clientReference;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JsonAdaptedCardDetails cardDetails;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String creditScore;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String debt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String lastModifiedAt;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("clientReference") String clientReference,
            @JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("cardDetails") JsonAdaptedCardDetails cardDetails,
            @JsonProperty("creditScore") String creditScore, @JsonProperty("debt") String debt,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("lastModifiedAt") String lastModifiedAt) {
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Creates a person in the older JSON format without timestamps. */
    public JsonAdaptedPerson(String clientReference, String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags, null, null);
    }

    /** Creates an unreferenced person from the fields used by older data files. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(null, name, phone, email, address, cardDetails, creditScore, debt, tags);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        clientReference = source.getClientReference() == null ? null : source.getClientReference().value;
        name = source.getName().fullName;
        phone = source.getPhone().map(value -> value.value).orElse(null);
        email = source.getEmail().map(value -> value.value).orElse(null);
        address = source.getAddress().map(value -> value.value).orElse(null);
        cardDetails = source.getCardDetails().map(JsonAdaptedCardDetails::new).orElse(null);
        creditScore = source.getCreditScore().map(Object::toString).orElse(null);
        debt = source.getDebt().map(Object::toString).orElse(null);
        createdAt = source.getCreatedAt() == null ? null : source.getCreatedAt().toString();
        lastModifiedAt = source.getLastModifiedAt() == null ? null : source.getLastModifiedAt().toString();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final Instant modelCreatedAt = parseTimestamp(createdAt);
        final Instant modelLastModifiedAt = parseTimestamp(lastModifiedAt);
        final ClientReference modelReference;
        if (clientReference == null) {
            modelReference = null;
        } else if (!ClientReference.isValidClientReference(clientReference)) {
            throw new IllegalValueException(ClientReference.MESSAGE_CONSTRAINTS);
        } else {
            modelReference = new ClientReference(clientReference);
        }

        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        // Files saved before card and financial details were introduced remain valid.
        if (cardDetails == null && creditScore == null && debt == null) {
            if (modelReference == null) {
                return new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags)
                        .withTimestamps(modelCreatedAt, modelLastModifiedAt);
            }
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CardDetails.class.getSimpleName()));
        }
        if (cardDetails == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CardDetails.class.getSimpleName()));
        }
        if (creditScore == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CreditScore.class.getSimpleName()));
        }
        if (debt == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Debt.class.getSimpleName()));
        }
        try {
            if (modelReference != null) {
                return new Person(modelReference, modelName, modelPhone, modelEmail, modelAddress,
                        cardDetails.toModelType(), new CreditScore(creditScore), new Debt(debt), modelTags)
                        .withTimestamps(modelCreatedAt, modelLastModifiedAt);
            }
            return new Person(modelName, modelPhone, modelEmail, modelAddress, cardDetails.toModelType(),
                    new CreditScore(creditScore), new Debt(debt), modelTags)
                    .withTimestamps(modelCreatedAt, modelLastModifiedAt);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private static Instant parseTimestamp(String value) throws IllegalValueException {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(INVALID_TIMESTAMP_MESSAGE);
        }
    }

}
