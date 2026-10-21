package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
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

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

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

        Person stamped = editedPerson.withTimestamps(target.getCreatedAt(), clock.instant());
        addressBook.setPerson(target, stamped);
        timestampChange = new TimestampChange(TimestampAction.EDIT, referenceOf(stamped),
                stamped.getCreatedAt(), stamped.getLastModifiedAt());
    }

    /** Returns the latest staged timestamp change, if this model made one. */
    public Optional<TimestampChange> getTimestampChange() {
        return Optional.ofNullable(timestampChange);
    }

    private static String referenceOf(Person person) {
        return person.getClientReference() == null ? null : person.getClientReference().value;
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
        displayedPersons.setComparator(sortOrder.comparator());
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

}
