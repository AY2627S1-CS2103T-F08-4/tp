package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addClient_savesToFile() throws Exception {
        CommandResult result = logic.execute("add r/c0001 n/Alex Yeoh p/87438807"
                + " e/alex@example.com a/10 Main Street cn/4111111111111111 cvv/123 exp/12/28"
                + " provider/Visa cs/700 d/1250.50");
        assertEquals("Added client: C0001 Alex Yeoh", result.getFeedbackToUser());
        assertEquals("C0001", model.getAddressBook().getPersonList().getFirst().getClientReference().value);
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        assertTrue(Files.exists(dataPath));
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
    }

    @Test
    public void execute_addClient_overwritesExistingSavedData() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        String original = "{\"persons\": []}";
        Files.writeString(dataPath, original);
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        assertFalse(original.equals(Files.readString(dataPath)));
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_displayCommand_doesNotSave() throws Exception {
        logic.execute("list");
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
    }

    @Test
    public void execute_findByPhone_filtersWithoutSaving() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        logic.execute("add r/C0002 n/Bernice Yu p/87438808 e/bernice@example.com a/20 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        String savedJson = Files.readString(dataPath);

        CommandResult result = logic.execute("find p/87438808");
        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals("C0002", logic.getFilteredPersonList().getFirst().getClientReference().value);
        assertEquals(savedJson, Files.readString(dataPath));
    }

    @Test
    public void execute_addThenDelete_syncsCurrentData() throws Exception {
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        String expectedFeedback = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(model.getFilteredPersonList().getFirst()));
        CommandResult result = logic.execute("delete 1");
        assertEquals(expectedFeedback, result.getFeedbackToUser());
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().orElseThrow();
        assertTrue(loaded.getPersonList().isEmpty());
    }

    @Test
    public void execute_deleteFromFilteredList_removesDisplayedClient() throws Exception {
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        logic.execute("add r/C0002 n/Bernice Yu p/87438808 e/bernice@example.com a/20 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        logic.execute("find n/Bernice");
        logic.execute("delete 1");

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().orElseThrow();
        assertEquals(1, loaded.getPersonList().size());
        assertEquals("C0001", loaded.getPersonList().getFirst().getClientReference().value);
    }

    @Test
    public void execute_editThenClear_syncsCurrentData() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        logic.execute("edit 1 p/91234567");
        ReadOnlyAddressBook edited = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow();
        assertEquals("91234567", edited.getPersonList().getFirst().getPhone().value);
        assertEquals("C0001", edited.getPersonList().getFirst().getClientReference().value);

        logic.execute("clear");
        ReadOnlyAddressBook cleared = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow();
        assertTrue(cleared.getPersonList().isEmpty());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, LogicManager.SAVE_FAILURE_MESSAGE);
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, LogicManager.SAVE_FAILURE_MESSAGE);
    }

    @Test
    public void execute_failedSave_keepsModelAndFilteredList() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");
        String savedJson = Files.readString(dataPath);
        logic.execute("find n/Alex");
        Model before = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        before.updateFilteredPersonList(model.getFilteredPersonListPredicate());

        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("write failed");
            }
        };
        StorageManager storage = new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);

        assertThrows(CommandException.class, LogicManager.SAVE_FAILURE_MESSAGE, () -> logic.execute("delete 1"));
        assertEquals(before, model);
        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals(savedJson, Files.readString(dataPath));
    }

    @Test
    public void execute_protectedMode_blocksChangesAndAllowsDisplayCommands() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        Files.writeString(dataPath, "invalid json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage, LogicManager.LOAD_FAILURE_MESSAGE, true);

        assertThrows(CommandException.class, LogicManager.LOAD_FAILURE_MESSAGE, () -> logic.execute("clear"));
        assertThrows(CommandException.class, LogicManager.LOAD_FAILURE_MESSAGE, () ->
                logic.execute("edit 1 p/91234567"));
        assertThrows(CommandException.class, LogicManager.LOAD_FAILURE_MESSAGE, () -> logic.execute("delete 1"));
        assertThrows(CommandException.class, LogicManager.LOAD_FAILURE_MESSAGE, () ->
                logic.execute("add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street"
                        + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50"));
        assertEquals(ListCommand.MESSAGE_SUCCESS, logic.execute("list").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("invalid json", Files.readString(dataPath));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        assertCommandFailure("clear", CommandException.class, expectedMessage, new ModelManager());
    }
}
