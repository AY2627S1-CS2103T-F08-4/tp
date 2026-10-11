package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: contact details are present, field values are validated, immutable.
 * Legacy profiles may have no card or financial details.
 */
public class Person {

    // Identity fields
    private final ClientReference clientReference;
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final CardDetails cardDetails;
    private final CreditScore creditScore;
    private final Debt debt;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Creates a legacy profile without card or financial details.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, tags);
        this.clientReference = null;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = null;
        this.creditScore = null;
        this.debt = null;
        this.tags.addAll(tags);
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, CardDetails cardDetails,
            CreditScore creditScore, Debt debt, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, cardDetails, creditScore, debt, tags);
        this.clientReference = null;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.tags.addAll(tags);
    }

    /**
     * Creates a referenced client while retaining its card, financial, and tag details.
     */
    public Person(ClientReference clientReference, Name name, Phone phone, Email email, Address address,
            CardDetails cardDetails, CreditScore creditScore, Debt debt, Set<Tag> tags) {
        requireAllNonNull(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags);
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.tags.addAll(tags);
    }

    public ClientReference getClientReference() {
        return clientReference;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /** Returns the card details, or null for a legacy profile. */
    public CardDetails getCardDetails() {
        return cardDetails;
    }

    /** Returns the credit score, or null for a legacy profile. */
    public CreditScore getCreditScore() {
        return creditScore;
    }

    /** Returns the debt, or null for a legacy profile. */
    public Debt getDebt() {
        return debt;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * US01 clients share an identity only when their normalized references match.
     * Existing records without references retain their name-based identity.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        if (otherPerson == null) {
            return false;
        }
        if (clientReference != null || otherPerson.clientReference != null) {
            return Objects.equals(clientReference, otherPerson.clientReference);
        }
        return otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return Objects.equals(clientReference, otherPerson.clientReference)
                && name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && Objects.equals(cardDetails, otherPerson.cardDetails)
                && Objects.equals(creditScore, otherPerson.creditScore)
                && Objects.equals(debt, otherPerson.debt)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .toString();
    }

}
