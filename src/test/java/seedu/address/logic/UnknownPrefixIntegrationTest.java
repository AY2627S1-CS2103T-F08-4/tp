package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class UnknownPrefixIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void edit_unknownPrefix_preservesModelAndFile() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().build());
        Path dataPath = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataPath);
        storage.saveAddressBook(original);
        String savedJson = Files.readString(dataPath);
        ModelManager model = new ModelManager(original, new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        ParseException error = assertThrows(ParseException.class, () ->
                logic.execute("edit 1 a/10 Main Street ph/92345678"));

        assertEquals("Unknown prefix: ph/", error.getMessage());
        assertEquals(original, model.getAddressBook());
        assertEquals(savedJson, Files.readString(dataPath));
    }
}
