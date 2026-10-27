package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CREDIT_SCORE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CVV;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPIRY_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROVIDER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REFERENCE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SERVICE_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
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
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_REFERENCE + "REFERENCE] "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_CARD_NUMBER + "CARD_NUMBER] "
            + "[" + PREFIX_CVV + "CVV] "
            + "[" + PREFIX_EXPIRY_DATE + "EXPIRY_DATE] "
            + "[" + PREFIX_PROVIDER + "PROVIDER] "
            + "[" + PREFIX_CREDIT_SCORE + "CREDIT_SCORE] "
            + "[" + PREFIX_DEBT + "DEBT] "
            + "[" + PREFIX_SERVICE_NOTE + "SERVICE_NOTE] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    public static final String MESSAGE_INCOMPLETE_CARD_DETAILS =
            "A person without card details must provide all card fields when adding a card.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return CommandResult.selectPerson(String.format(MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson)), editedPerson);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        assert personToEdit != null : "An edit target must exist before reconstruction";

        ClientReference updatedReference = editPersonDescriptor.getClientReference()
                .or(personToEdit::getClientReference).orElse(null);
        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().or(personToEdit::getPhone).orElse(null);
        Email updatedEmail = editPersonDescriptor.getEmail().or(personToEdit::getEmail).orElse(null);
        Address updatedAddress = editPersonDescriptor.getAddress().or(personToEdit::getAddress).orElse(null);
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        CardDetails updatedCardDetails = mergeCardDetails(personToEdit, editPersonDescriptor);
        CreditScore updatedCreditScore = editPersonDescriptor.getCreditScore()
                .or(personToEdit::getCreditScore).orElse(null);
        Debt updatedDebt = editPersonDescriptor.getDebt().or(personToEdit::getDebt).orElse(null);
        ServiceNote updatedServiceNote = editPersonDescriptor.isServiceNoteEdited()
                ? editPersonDescriptor.getServiceNote().orElse(null)
                : personToEdit.getServiceNote().orElse(null);

        return personToEdit.withDetails(updatedReference, updatedName, updatedPhone, updatedEmail,
                updatedAddress, updatedCardDetails, updatedCreditScore, updatedDebt, updatedTags, updatedServiceNote);
    }

    /** Merges edited card fields with an existing complete card. */
    private static CardDetails mergeCardDetails(Person current, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        CardDetails existing = current.getCardDetails().orElse(null);
        boolean isCardFieldEdited = editPersonDescriptor.getCardNumber().isPresent()
                || editPersonDescriptor.getCvv().isPresent()
                || editPersonDescriptor.getExpiryDate().isPresent()
                || editPersonDescriptor.getProvider().isPresent();
        if (!isCardFieldEdited) {
            return existing;
        }
        String number = editPersonDescriptor.getCardNumber()
                .orElseGet(() -> existing == null ? null : existing.getCardNumber());
        String cvv = editPersonDescriptor.getCvv()
                .orElseGet(() -> existing == null ? null : existing.getCvv());
        String expiry = editPersonDescriptor.getExpiryDate()
                .orElseGet(() -> existing == null ? null : existing.getExpiryDate());
        String provider = editPersonDescriptor.getProvider()
                .orElseGet(() -> existing == null ? null : existing.getProvider());
        if (number == null || cvv == null || expiry == null || provider == null) {
            throw new CommandException(MESSAGE_INCOMPLETE_CARD_DETAILS);
        }
        return new CardDetails(number, cvv, expiry, provider);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;
        private ClientReference clientReference;
        private String cardNumber;
        private String cvv;
        private String expiryDate;
        private String provider;
        private CreditScore creditScore;
        private Debt debt;
        private ServiceNote serviceNote;
        private boolean serviceNoteEdited;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setClientReference(toCopy.clientReference);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
            setCardNumber(toCopy.cardNumber);
            setCvv(toCopy.cvv);
            setExpiryDate(toCopy.expiryDate);
            setProvider(toCopy.provider);
            setCreditScore(toCopy.creditScore);
            setDebt(toCopy.debt);
            setServiceNote(toCopy.serviceNote);
            serviceNoteEdited = toCopy.serviceNoteEdited;
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(clientReference, name, phone, email, address,
                    cardNumber, cvv, expiryDate, provider, creditScore, debt, tags)
                    || serviceNoteEdited;
        }

        public void setClientReference(ClientReference clientReference) {
            this.clientReference = clientReference;
        }

        public Optional<ClientReference> getClientReference() {
            return Optional.ofNullable(clientReference);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public Optional<String> getCardNumber() {
            return Optional.ofNullable(cardNumber);
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        public Optional<String> getCvv() {
            return Optional.ofNullable(cvv);
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public Optional<String> getExpiryDate() {
            return Optional.ofNullable(expiryDate);
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Optional<String> getProvider() {
            return Optional.ofNullable(provider);
        }

        public void setCreditScore(CreditScore creditScore) {
            this.creditScore = creditScore;
        }

        public Optional<CreditScore> getCreditScore() {
            return Optional.ofNullable(creditScore);
        }

        public void setDebt(Debt debt) {
            this.debt = debt;
        }

        public Optional<Debt> getDebt() {
            return Optional.ofNullable(debt);
        }

        public void setServiceNote(ServiceNote serviceNote) {
            this.serviceNote = serviceNote;
            this.serviceNoteEdited = true;
        }

        public Optional<ServiceNote> getServiceNote() {
            return Optional.ofNullable(serviceNote);
        }

        public boolean isServiceNoteEdited() {
            return serviceNoteEdited;
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(clientReference, otherEditPersonDescriptor.clientReference)
                    && Objects.equals(cardNumber, otherEditPersonDescriptor.cardNumber)
                    && Objects.equals(cvv, otherEditPersonDescriptor.cvv)
                    && Objects.equals(expiryDate, otherEditPersonDescriptor.expiryDate)
                    && Objects.equals(provider, otherEditPersonDescriptor.provider)
                    && Objects.equals(creditScore, otherEditPersonDescriptor.creditScore)
                    && Objects.equals(debt, otherEditPersonDescriptor.debt)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && Objects.equals(serviceNote, otherEditPersonDescriptor.serviceNote)
                    && serviceNoteEdited == otherEditPersonDescriptor.serviceNoteEdited;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, phone, email, address, clientReference, cardNumber, cvv,
                    expiryDate, provider, creditScore, debt, tags, serviceNote, serviceNoteEdited);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("clientReference", clientReference)
                    .add("cardNumber", cardNumber)
                    .add("cvv", cvv)
                    .add("expiryDate", expiryDate)
                    .add("provider", provider)
                    .add("creditScore", creditScore)
                    .add("debt", debt)
                    .add("serviceNote", serviceNote)
                    .add("tags", tags)
                    .toString();
        }
    }
}
