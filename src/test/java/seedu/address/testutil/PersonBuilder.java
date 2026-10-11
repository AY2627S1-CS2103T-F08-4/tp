package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;

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
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";
    public static final String DEFAULT_CARD_NUMBER = "4111111111111111";
    public static final String DEFAULT_CVV = "123";
    public static final String DEFAULT_EXPIRY_DATE = "12/28";
    public static final String DEFAULT_PROVIDER = "Visa";
    public static final String DEFAULT_CREDIT_SCORE = "700";
    public static final String DEFAULT_DEBT = "0.00";

    private Name name;
    private ClientReference clientReference;
    private Phone phone;
    private Email email;
    private Address address;
    private CardDetails cardDetails;
    private CreditScore creditScore;
    private Debt debt;
    private Set<Tag> tags;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        cardDetails = new CardDetails(DEFAULT_CARD_NUMBER, DEFAULT_CVV, DEFAULT_EXPIRY_DATE, DEFAULT_PROVIDER);
        creditScore = new CreditScore(DEFAULT_CREDIT_SCORE);
        debt = new Debt(DEFAULT_DEBT);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        clientReference = personToCopy.getClientReference();
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        cardDetails = personToCopy.getCardDetails();
        creditScore = personToCopy.getCreditScore();
        debt = personToCopy.getDebt();
        tags = new HashSet<>(personToCopy.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /** Sets the credit score of the person being built. */
    public PersonBuilder withCreditScore(String creditScore) {
        this.creditScore = new CreditScore(creditScore);
        return this;
    }

    /** Sets the debt of the person being built. */
    public PersonBuilder withDebt(String debt) {
        this.debt = new Debt(debt);
        return this;
    }

    /** Sets all four card details of the person being built. */
    public PersonBuilder withCardDetails(String cardNumber, String cvv, String expiryDate, String provider) {
        cardDetails = new CardDetails(cardNumber, cvv, expiryDate, provider);
        return this;
    }

    /**
     * Sets the reference when building a US01 client.
     */
    public PersonBuilder withClientReference(String reference) {
        clientReference = new ClientReference(reference);
        return this;
    }

    /** Builds a US01 client or an existing record with its original financial details. */
    public Person build() {
        if (clientReference != null) {
            return new Person(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags);
        }
        if (cardDetails == null && creditScore == null && debt == null) {
            return new Person(name, phone, email, address, tags);
        }
        return new Person(name, phone, email, address, cardDetails, creditScore, debt, tags);
    }

}
