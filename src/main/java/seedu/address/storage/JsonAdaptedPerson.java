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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

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
import seedu.address.model.person.ServiceNote;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String phone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JsonNode serviceNote;
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
            @JsonProperty("lastModifiedAt") String lastModifiedAt,
            @JsonProperty("serviceNote") JsonNode serviceNote) {
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.serviceNote = serviceNote;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Creates a person in the older JSON format without timestamps. */
    public JsonAdaptedPerson(String clientReference, String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags, null, null, null);
    }

    /** Creates a person with timestamps and no service note. */
    public JsonAdaptedPerson(String clientReference, String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags,
            String createdAt, String lastModifiedAt) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags,
                createdAt, lastModifiedAt, null);
    }

    /** Creates an unreferenced person from the fields used by older data files. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(null, name, phone, email, address, cardDetails, creditScore, debt, tags, null, null, null);
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
        serviceNote = source.getServiceNote() == null ? null : TextNode.valueOf(source.getServiceNote().value);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     * Absent or null optional details remain unrecorded; supplied details must be valid.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final ServiceNote modelNote;
        // Jackson also supplies NullNode when an optional creator property is absent.
        // JsonAddressBookStorage rejects explicit JSON null before reaching this adapter.
        if (serviceNote == null || serviceNote.isNull()) {
            modelNote = null;
        } else if (!serviceNote.isTextual()
                || !ServiceNote.isValidServiceNote(serviceNote.textValue())) {
            throw new IllegalValueException(ServiceNote.MESSAGE_CONSTRAINTS);
        } else {
            modelNote = new ServiceNote(serviceNote.textValue());
        }
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

        if (phone != null && !Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = phone == null ? null : new Phone(phone);

        if (email != null && !Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = email == null ? null : new Email(email);

        if (address != null && !Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = address == null ? null : new Address(address);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        try {
            final CardDetails modelCardDetails = cardDetails == null ? null : cardDetails.toModelType();
            final CreditScore modelCreditScore = creditScore == null ? null : new CreditScore(creditScore);
            final Debt modelDebt = debt == null ? null : new Debt(debt);
            if (modelReference != null) {
                return new Person(modelReference, modelName, modelPhone, modelEmail, modelAddress,
                        modelCardDetails, modelCreditScore, modelDebt, modelTags, modelNote)
                        .withTimestamps(modelCreatedAt, modelLastModifiedAt);
            }
            return new Person(modelName, modelPhone, modelEmail, modelAddress, modelCardDetails,
                    modelCreditScore, modelDebt, modelTags, modelNote)
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
