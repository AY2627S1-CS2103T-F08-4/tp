package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the remark command's interaction with the model.
 */
public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addRemark_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Remark remark = new Remark("Likes to swim");
        Person editedPerson = new PersonBuilder(original).withRemark(remark.value).build();
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(remark, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getRemark());
    }

    @Test
    public void execute_removeRemark_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Remark emptyRemark = new Remark("");
        Person editedPerson = new PersonBuilder(original).withRemark("").build();
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, emptyRemark);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(emptyRemark, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getRemark());
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonAndResetsFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Remark remark = new Remark("Met at conference");
        Person editedPerson = new PersonBuilder(original).withRemark(remark.value).build();
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(remark, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()).getRemark());
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand command = new RemarkCommand(outOfBounds, new Remark("Likes to swim"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        command = new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes to swim"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes to swim"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Other"))));
        assertFalse(command.equals(null));
    }
}
