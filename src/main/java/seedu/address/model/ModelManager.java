package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.ClientClash;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    /** The kind of timestamp change made by a data-changing command. */
    public enum TimestampAction { ADD, EDIT }

    /** Timestamp values to log once the staged change has been saved. */
    public record TimestampChange(TimestampAction action, String reference,
            Instant createdAt, Instant lastModifiedAt) { }

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> displayedPersons;
    private SortOrder sortOrder = SortOrder.ORIGINAL;
    private final Clock clock;
    private TimestampChange timestampChange;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, Clock.systemUTC());
    }

    /** Initializes a ModelManager with an injectable clock for record timestamps. */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, Clock clock) {
        requireAllNonNull(addressBook, userPrefs, clock);

        logger.fine("Initializing address book model");

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        displayedPersons = new SortedList<>(filteredPersons);
        this.clock = clock;
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    /**
     * Finds fields in {@code proposedClient} that have the same values as
     * fields belonging to another client in the address book.
     *
     * <p>The reference number, phone number, card number, and email address
     * are checked independently. A proposed client may therefore produce
     * multiple clashes with one or more existing clients.
     *
     * @param proposedClient the client that is about to be added or saved
     * @param clientBeingEdited the existing client being edited, or
     *                          {@code null} when adding a new client
     * @return all detected clashes
     */
    @Override
    public List<ClientClash> findClientClashes(
            Person proposedClient,
            Person clientBeingEdited) {

        requireNonNull(proposedClient);

        List<ClientClash> clashes = new ArrayList<>();

        for (Person existingClient : addressBook.getPersonList()) {
            /*
             * During editing, ignore the original client. Otherwise an
             * unchanged field would clash with the client itself.
             */
            if (existingClient.equals(clientBeingEdited)) {
                continue;
            }

            addPhoneClashIfPresent(proposedClient, existingClient, clashes);
            addCardNumberClashIfPresent(proposedClient, existingClient, clashes);
            addEmailClashIfPresent(proposedClient, existingClient, clashes);
        }

        return clashes;
    }

    /** Adds a phone-number clash when both clients have the same phone number. */
    private static void addPhoneClashIfPresent(
            Person proposedClient,
            Person existingClient,
            List<ClientClash> clashes) {

        if (proposedClient.getPhone().isEmpty()
                || existingClient.getPhone().isEmpty()) {
            return;
        }

        String proposedPhone = proposedClient.getPhone().get().value;
        String existingPhone = existingClient.getPhone().get().value;

        if (proposedPhone.equals(existingPhone)) {
            clashes.add(new ClientClash(
                    ClientClash.Field.PHONE,
                    proposedPhone,
                    existingClient));
        }
    }

    /** Adds a card-number clash when both clients have the same card number. */
    private static void addCardNumberClashIfPresent(
            Person proposedClient,
            Person existingClient,
            List<ClientClash> clashes) {

        if (proposedClient.getCardDetails().isEmpty()
                || existingClient.getCardDetails().isEmpty()) {
            return;
        }

        String proposedCardNumber = proposedClient.getCardDetails()
                .get().getCardNumber();
        String existingCardNumber = existingClient.getCardDetails()
                .get().getCardNumber();

        if (proposedCardNumber.equals(existingCardNumber)) {
            clashes.add(new ClientClash(
                    ClientClash.Field.CARD_NUMBER,
                    proposedCardNumber,
                    existingClient));
        }
    }

    /** Adds an email-address clash when both clients have the same email. */
    private static void addEmailClashIfPresent(
            Person proposedClient,
            Person existingClient,
            List<ClientClash> clashes) {

        if (proposedClient.getEmail().isEmpty()
                || existingClient.getEmail().isEmpty()) {
            return;
        }

        String proposedEmail = proposedClient.getEmail().get().value;
        String existingEmail = existingClient.getEmail().get().value;

        if (proposedEmail.equals(existingEmail)) {
            clashes.add(new ClientClash(
                    ClientClash.Field.EMAIL,
                    proposedEmail,
                    existingClient));
        }
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        requireNonNull(person);
        Instant now = clock.instant();
        Person stamped = person.withTimestamps(now, now);
        addressBook.addPerson(stamped);
        timestampChange = new TimestampChange(TimestampAction.ADD, referenceOf(stamped), now, now);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        Person stamped = editedPerson.withTimestamps(target.getCreatedAt().orElse(null), clock.instant());
        addressBook.setPerson(target, stamped);
        timestampChange = new TimestampChange(TimestampAction.EDIT, referenceOf(stamped),
                stamped.getCreatedAt().orElse(null), stamped.getLastModifiedAt().orElse(null));
    }

    /** Returns the latest staged timestamp change, if this model made one. */
    public Optional<TimestampChange> getTimestampChange() {
        return Optional.ofNullable(timestampChange);
    }

    private static String referenceOf(Person person) {
        return person.getClientReference().map(reference -> reference.value).orElse(null);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return displayedPersons;
    }

    @Override
    public Predicate<Person> getFilteredPersonListPredicate() {
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        return predicate == null ? PREDICATE_SHOW_ALL_PERSONS : predicate::test;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = requireNonNull(sortOrder);
        displayedPersons.setComparator(sortOrder.getComparator());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && sortOrder == otherModelManager.sortOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressBook, userPrefs, filteredPersons, sortOrder);
    }

}
