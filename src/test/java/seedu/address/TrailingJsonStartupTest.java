package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class TrailingJsonStartupTest {
    @TempDir
    public Path testFolder;

    @Test
    public void load_trailingContent_blocksChangesAndPreservesOriginalFile() throws Exception {
        Path dataPath = testFolder.resolve("trailing.json");
        AddressBook saved = new AddressBook();
        saved.addPerson(new PersonBuilder().withClientReference("ALPHABASE").build());
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataPath);
        addressBookStorage.saveAddressBook(saved);
        String validJson = Files.readString(dataPath);
        for (String tail : new String[] {"\nTHIS IS NOT JSON", "\n{}", "\nnull"}) {
            String corrupted = validJson + tail;
            Files.writeString(dataPath, corrupted);
            MainApp.InitialData initialData = load(dataPath);
            assertTrue(initialData.isProtectedMode());
            assertEquals(LogicManager.LOAD_FAILURE_MESSAGE, initialData.message());
            StorageManager storage = new StorageManager(addressBookStorage,
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
            LogicManager logic = new LogicManager(initialData.model(), storage,
                    initialData.message(), initialData.isProtectedMode());

            assertThrows(CommandException.class, () -> logic.execute("add r/ALPHAEXTRA n/Extra Client"));
            assertEquals(corrupted, Files.readString(dataPath));
        }
    }

    private MainApp.InitialData load(Path dataPath) {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        return new MainApp().initModelManager(storage, new UserPrefs());
    }
}
