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
    private final ServiceNote serviceNote;
    private final Set<Tag> tags = new HashSet<>();
    private final Instant createdAt;
    private final Instant lastModifiedAt;

    /**
     * Creates a legacy profile without card or financial details.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, null);
    }

    /** Creates a legacy profile with an optional service note. */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, ServiceNote serviceNote) {
        this(null, name, phone, email, address, null, null, null, tags, serviceNote, null, null);
    }

    /**
     * Name and tags must be non-null; contact, card, and financial details may be absent.
     */
    public Person(Name name, Phone phone, Email email, Address address, CardDetails cardDetails,
            CreditScore creditScore, Debt debt, Set<Tag> tags) {
        this(name, phone, email, address, cardDetails, creditScore, debt, tags, null);
    }

    /** Creates an unreferenced profile with card details and an optional service note. */
    public Person(Name name, Phone phone, Email email, Address address, CardDetails cardDetails,
            CreditScore creditScore, Debt debt, Set<Tag> tags, ServiceNote serviceNote) {
        this(null, name, phone, email, address, cardDetails, creditScore, debt, tags, serviceNote, null, null);
    }

    /**
     * Creates a referenced client. Card details may be absent after a card is removed.
     */
    public Person(ClientReference clientReference, Name name, Phone phone, Email email, Address address,
            CardDetails cardDetails, CreditScore creditScore, Debt debt, Set<Tag> tags) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags, null);
    }

    /** Creates a referenced client with an optional service note. */
    public Person(ClientReference clientReference, Name name, Phone phone, Email email, Address address,
            CardDetails cardDetails, CreditScore creditScore, Debt debt, Set<Tag> tags, ServiceNote serviceNote) {
        this(Objects.requireNonNull(clientReference), name, phone, email, address, cardDetails,
                creditScore, debt, tags, serviceNote, null, null);
    }

    private Person(ClientReference clientReference, Name name, Phone phone, Email email, Address address,
            CardDetails cardDetails, CreditScore creditScore, Debt debt, Set<Tag> tags, ServiceNote serviceNote,
            Instant createdAt, Instant lastModifiedAt) {
        requireAllNonNull(name, tags);
        requireAllNonNull(tags);
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.serviceNote = serviceNote;
        this.tags.addAll(tags);
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    /** Returns a copy with the given timestamps; missing legacy timestamps may be null. */
    public Person withTimestamps(Instant createdAt, Instant lastModifiedAt) {
        return new Person(clientReference, name, phone, email, address, cardDetails, creditScore, debt,
                tags, serviceNote, createdAt, lastModifiedAt);
    }

    /** Returns a copy with a changed service note. */
    public Person withServiceNote(ServiceNote note) {
        return new Person(clientReference, name, phone, email, address, cardDetails, creditScore, debt,
                tags, note, createdAt, lastModifiedAt);
    }

    /** Returns a copy without card details. */
    public Person withoutCardDetails() {
        return new Person(clientReference, name, phone, email, address, null, creditScore, debt,
                tags, serviceNote, createdAt, lastModifiedAt);
    }

    /** Returns a copy with supplied fields while preserving this person's timestamps. */
    public Person withDetails(ClientReference reference, Name newName, Phone newPhone, Email newEmail,
            Address newAddress, CardDetails newCard, CreditScore newScore, Debt newDebt,
            Set<Tag> newTags, ServiceNote newNote) {
        return new Person(reference, newName, newPhone, newEmail, newAddress, newCard, newScore,
                newDebt, newTags, newNote, createdAt, lastModifiedAt);
    }

    public Optional<ClientReference> getClientReference() {
        return Optional.ofNullable(clientReference);
    }

    public Name getName() {
        return name;
    }

    public Optional<Phone> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
    }

    /** Returns the card details, empty if the profile has no card. */
    public Optional<CardDetails> getCardDetails() {
        return Optional.ofNullable(cardDetails);
    }

    /** Returns the credit score, empty when not recorded. */
    public Optional<CreditScore> getCreditScore() {
        return Optional.ofNullable(creditScore);
    }

    /** Returns the debt, empty when not recorded. */
    public Optional<Debt> getDebt() {
        return Optional.ofNullable(debt);
    }

    public Optional<ServiceNote> getServiceNote() {
        return Optional.ofNullable(serviceNote);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /** Returns the creation instant, or empty if it was not recorded. */
    public Optional<Instant> getCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    /** Returns the most recent edit instant, or empty if it was not recorded. */
    public Optional<Instant> getLastModifiedAt() {
        return Optional.ofNullable(lastModifiedAt);
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
                && Objects.equals(serviceNote, otherPerson.serviceNote)
                && tags.equals(otherPerson.tags)
                && Objects.equals(createdAt, otherPerson.createdAt)
                && Objects.equals(lastModifiedAt, otherPerson.lastModifiedAt);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags,
                createdAt, lastModifiedAt, serviceNote);
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
