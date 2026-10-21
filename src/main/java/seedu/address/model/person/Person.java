package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: name and tags are non-null, supplied field values are validated, immutable.
 * Contact, card, and financial details may be absent independently. A present card contains all four card fields.
 * Referenced clients require a client reference; legacy profiles may remain unreferenced.
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
    private final Instant createdAt;
    private final Instant lastModifiedAt;

    /**
     * Creates an unreferenced legacy profile without card or financial details.
     * Name and tags must be non-null; contact details may be null when not recorded.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, null, null, null, tags);
    }

    /**
     * Creates an unreferenced legacy profile, retaining any recorded details.
     * Name and tags must be non-null; contact, card, and financial details may each be null when not recorded.
     */
    public Person(Name name, Phone phone, Email email, Address address, CardDetails cardDetails,
            CreditScore creditScore, Debt debt, Set<Tag> tags) {
        requireAllNonNull(name, tags);
        this.clientReference = null;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.tags.addAll(tags);
        this.createdAt = null;
        this.lastModifiedAt = null;
    }

    /**
     * Creates a referenced client with a non-null reference, name, and tag set.
     * Contact, card, and financial details may each be null when not recorded.
     */
    public Person(ClientReference clientReference, Name name, Phone phone, Email email, Address address,
            CardDetails cardDetails, CreditScore creditScore, Debt debt, Set<Tag> tags) {
        requireAllNonNull(clientReference, name, tags);
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.tags.addAll(tags);
        this.createdAt = null;
        this.lastModifiedAt = null;
    }

    private Person(Person source, Instant createdAt, Instant lastModifiedAt) {
        this.clientReference = source.clientReference;
        this.name = source.name;
        this.phone = source.phone;
        this.email = source.email;
        this.address = source.address;
        this.cardDetails = source.cardDetails;
        this.creditScore = source.creditScore;
        this.debt = source.debt;
        this.tags.addAll(source.tags);
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    /** Returns a copy with the given timestamps; missing legacy timestamps may be null. */
    public Person withTimestamps(Instant createdAt, Instant lastModifiedAt) {
        return new Person(this, createdAt, lastModifiedAt);
    }

    public ClientReference getClientReference() {
        return clientReference;
    }

    public Name getName() {
        return name;
    }

    /** Returns a non-null Optional, empty when no phone is recorded. */
    public Optional<Phone> getPhone() {
        return Optional.ofNullable(phone);
    }

    /** Returns a non-null Optional, empty when no email is recorded. */
    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    /** Returns a non-null Optional, empty when no address is recorded. */
    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
    }

    /** Returns a non-null Optional, empty when no card details are recorded. */
    public Optional<CardDetails> getCardDetails() {
        return Optional.ofNullable(cardDetails);
    }

    /** Returns a non-null Optional, empty when no credit score is recorded. */
    public Optional<CreditScore> getCreditScore() {
        return Optional.ofNullable(creditScore);
    }

    /** Returns a non-null Optional, empty when no debt is recorded. */
    public Optional<Debt> getDebt() {
        return Optional.ofNullable(debt);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /** Returns the creation instant, or null if it was not recorded. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Returns the most recent edit instant, or null if it was not recorded. */
    public Instant getLastModifiedAt() {
        return lastModifiedAt;
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
                && Objects.equals(phone, otherPerson.phone)
                && Objects.equals(email, otherPerson.email)
                && Objects.equals(address, otherPerson.address)
                && Objects.equals(cardDetails, otherPerson.cardDetails)
                && Objects.equals(creditScore, otherPerson.creditScore)
                && Objects.equals(debt, otherPerson.debt)
                && tags.equals(otherPerson.tags)
                && Objects.equals(createdAt, otherPerson.createdAt)
                && Objects.equals(lastModifiedAt, otherPerson.lastModifiedAt);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags,
                createdAt, lastModifiedAt);
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
