package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.model.ModelManager;
import seedu.address.model.person.ClientClash;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class EmailClashIntegrationTest {
    @TempDir
    public Path testFolder;

    private ModelManager model;
    private LogicManager logic;
    private Path dataPath;

    @BeforeEach
    void setUp() throws Exception {
        model = new ModelManager();
        dataPath = testFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);
        logic.execute("add r/EMAILA n/Alice Tan e/alice@example.com");
    }

    @Test
    void add_domainCaseDifference_requestsConfirmationWithoutSaving() throws Exception {
        String before = Files.readString(dataPath);
        CommandResult result = logic.execute("add r/EMAILB n/Bob Lim e/alice@EXAMPLE.COM");

        assertTrue(result.requiresClashConfirmation());
        assertEquals(1, result.getClientClashes().size());
        assertEquals(ClientClash.Field.EMAIL, result.getClientClashes().getFirst().field());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(before, Files.readString(dataPath));
    }

    @Test
    void edit_domainCaseDifference_requestsConfirmationWithoutSaving() throws Exception {
        logic.execute("add r/EMAILB n/Bob Lim e/bob@example.com");
        String before = Files.readString(dataPath);
        CommandResult result = logic.execute("edit 2 e/alice@Example.Com");

        assertTrue(result.requiresClashConfirmation());
        assertEquals("bob@example.com", model.getFilteredPersonList().get(1).getEmail().orElseThrow().value);
        assertEquals(before, Files.readString(dataPath));
    }

    @Test
    void add_confirmedDomainClash_savesOriginalCapitalization() throws Exception {
        String command = "add r/EMAILB n/Bob Lim e/alice@EXAMPLE.COM";
        assertTrue(logic.execute(command).requiresClashConfirmation());

        assertFalse(logic.execute(command + " --confirm-clash").requiresClashConfirmation());
        assertEquals("alice@EXAMPLE.COM", new JsonAddressBookStorage(dataPath).readAddressBook()
                .orElseThrow().getPersonList().get(1).getEmail().orElseThrow().value);
    }

    @Test
    void add_localPartCaseDifference_doesNotClash() throws Exception {
        CommandResult result = logic.execute("add r/EMAILB n/Bob Lim e/Alice@EXAMPLE.COM");

        assertFalse(result.requiresClashConfirmation());
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    void edit_ownDomainCase_doesNotClashWithSelf() throws Exception {
        assertFalse(logic.execute("edit 1 e/alice@Example.Com").requiresClashConfirmation());
        assertEquals("alice@Example.Com", model.getFilteredPersonList().getFirst().getEmail().orElseThrow().value);
    }
}
