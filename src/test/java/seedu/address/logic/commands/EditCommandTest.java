package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2026-10-12T06:18:00Z"), ZoneOffset.UTC);
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), TEST_CLOCK);

    @Test
    public void execute_legacyProfileWithoutCard_preservesIndependentFinancialDetails() {
        Person source = model.getFilteredPersonList().getFirst();
        Person legacy = new Person(source.getName(), null, null, null, null,
                new CreditScore("0"), new Debt("0.00"), source.getTags());
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(legacy);
        model = new ModelManager(addressBook, new UserPrefs(), TEST_CLOCK);
        Person editedPerson = new PersonBuilder(legacy).withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(legacy, editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_legacyProfile_preservesMissingCardAndFinancialDetails() {
        Person source = model.getFilteredPersonList().getFirst();
        Person legacy = new Person(source.getName(), source.getPhone().orElseThrow(), source.getEmail().orElseThrow(),
                source.getAddress().orElseThrow(), source.getTags());
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(legacy);
        model = new ModelManager(addressBook, new UserPrefs(), TEST_CLOCK);
        Person editedPerson = new PersonBuilder(legacy).withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build());
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(legacy, editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor, true);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor, true);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(lastPerson, editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_editOnlyCvv_preservesOtherCardFields() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withCvv("456").build(), true);
        command.execute(model);

        Person edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(original.getCardDetails().orElse(null).getCardNumber(),
                edited.getCardDetails().orElse(null).getCardNumber());
        assertEquals("456", edited.getCardDetails().orElse(null).getCvv());
        assertEquals(original.getCardDetails().orElse(null).getExpiryDate(),
                edited.getCardDetails().orElse(null).getExpiryDate());
        assertEquals(original.getCardDetails().orElse(null).getProvider(),
                edited.getCardDetails().orElse(null).getProvider());
    }

    @Test
    public void execute_editMultipleCardFields_preservesUneditedFields() throws Exception {
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder()
                        .withCardNumber("5555555555554444")
                        .withCvv("456")
                        .build());
        command.execute(model);

        Person edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("5555555555554444", edited.getCardDetails().orElse(null).getCardNumber());
        assertEquals("456", edited.getCardDetails().orElse(null).getCvv());
        assertEquals("12/28", edited.getCardDetails().orElse(null).getExpiryDate());
        assertEquals("Visa", edited.getCardDetails().orElse(null).getProvider());
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor(), true);
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(editedPerson, editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build(), true);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs(), TEST_CLOCK);
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertEquals(CommandResult.selectPerson(expectedMessage, editedPerson),
                assertDoesNotThrow(() -> editCommand.execute(model)));
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_clearAbsentNote_succeeds() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setServiceNote(null);
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        assertDoesNotThrow(() -> command.execute(model));
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(editedPerson.getServiceNote().isEmpty());
        assertEquals(person.getCreatedAt(), editedPerson.getCreatedAt());
        assertEquals(TEST_CLOCK.instant(), editedPerson.getLastModifiedAt().orElseThrow());
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));
        assertEquals(standardCommand.hashCode(), commandWithSameValues.hashCode());

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + ", clashConfirmed=false}";
        assertEquals(expected, editCommand.toString());
    }

}
