package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.SortOrder;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class SortCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void sort_changesDisplayOnlyAndListKeepsActiveOrder() throws Exception {
        Person mary = client("C3", "Mary O'Connor");
        Person alex = client("C1", "alex Yeoh");
        Person bernice = client("C2", "Bernice Yu");
        AddressBook addressBook = book(mary, alex, bernice);
        Path dataPath = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage jsonStorage = new JsonAddressBookStorage(dataPath);
        jsonStorage.saveAddressBook(addressBook);
        String savedJson = Files.readString(dataPath);
        CountingStorage storage = new CountingStorage(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        ModelManager model = new ModelManager(addressBook, new UserPrefs());
        LogicManager logic = new LogicManager(model, storage);

        assertEquals("Sorted 3 client(s) by name (A-Z).", logic.execute("sort").getFeedbackToUser());
        assertEquals(List.of(alex, bernice, mary), logic.getFilteredPersonList());
        assertEquals(SortOrder.ASC, model.getSortOrder());
        logic.execute("list");
        assertEquals(List.of(alex, bernice, mary), logic.getFilteredPersonList());
        assertEquals("Sorted 3 client(s) by name (Z-A).", logic.execute("sort desc").getFeedbackToUser());
        assertEquals(List.of(mary, bernice, alex), logic.getFilteredPersonList());
        assertThrows(ParseException.class, () -> logic.execute("sort name"));
        assertEquals(List.of(mary, bernice, alex), logic.getFilteredPersonList());
        assertEquals(SortCommand.MESSAGE_RESET, logic.execute("sort reset").getFeedbackToUser());
        assertEquals(List.of(mary, alex, bernice), logic.getFilteredPersonList());
        assertEquals(List.of(mary, alex, bernice), model.getAddressBook().getPersonList());
        assertEquals(0, storage.saveCount);
        assertEquals(savedJson, Files.readString(dataPath));
    }

    @Test
    public void sort_survivesFindAddEditDelete() throws Exception {
        Person mary = client("C3", "Mary O'Connor");
        Person alex = client("C1", "Alex Yeoh");
        Person bernice = client("C2", "Bernice Yu");
        AddressBook addressBook = book(mary, alex, bernice);
        Path dataPath = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage jsonStorage = new JsonAddressBookStorage(dataPath);
        ModelManager model = new ModelManager(addressBook, new UserPrefs());
        LogicManager logic = new LogicManager(model, new StorageManager(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        logic.execute("find n/Alex");
        assertEquals("Sorted 1 client(s) by name (A-Z).", logic.execute("sort").getFeedbackToUser());
        logic.execute("list");
        assertEquals(List.of(alex, bernice, mary), logic.getFilteredPersonList());

        Person aaron = client("C4", "Aaron Yu");
        logic.execute(PersonUtil.getAddCommand(aaron));
        assertEquals(List.of("Aaron Yu", "Alex Yeoh", "Bernice Yu", "Mary O'Connor"),
                logic.getFilteredPersonList().stream().map(p -> p.getName().fullName).toList());
        assertEquals(List.of("Mary O'Connor", "Alex Yeoh", "Bernice Yu", "Aaron Yu"),
                model.getAddressBook().getPersonList().stream().map(p -> p.getName().fullName).toList());

        logic.execute("edit 1 n/Zara Yu");
        assertEquals(List.of("Alex Yeoh", "Bernice Yu", "Mary O'Connor", "Zara Yu"),
                logic.getFilteredPersonList().stream().map(p -> p.getName().fullName).toList());
        logic.execute("delete 1");
        assertEquals(List.of("Bernice Yu", "Mary O'Connor", "Zara Yu"),
                logic.getFilteredPersonList().stream().map(p -> p.getName().fullName).toList());
        assertEquals(List.of("Mary O'Connor", "Bernice Yu", "Zara Yu"),
                model.getAddressBook().getPersonList().stream().map(p -> p.getName().fullName).toList());
    }

    @Test
    public void sort_isAllowedInProtectedModeAndEmptyOrderAppliesAfterAdd() throws Exception {
        Path dataPath = temporaryFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        ModelManager model = new ModelManager();
        LogicManager protectedLogic = new LogicManager(model, storage, "load failed", true);
        assertEquals(SortCommand.MESSAGE_RESET, protectedLogic.execute("sort reset").getFeedbackToUser());
        assertEquals(SortCommand.MESSAGE_EMPTY, protectedLogic.execute("sort desc").getFeedbackToUser());
        assertEquals(SortOrder.DESC, model.getSortOrder());
        assertFalse(Files.exists(dataPath));

        LogicManager normalLogic = new LogicManager(model, storage);
        normalLogic.execute(PersonUtil.getAddCommand(client("C1", "Amy Bee")));
        normalLogic.execute(PersonUtil.getAddCommand(client("C2", "Zara Yu")));
        assertEquals(List.of("Zara Yu", "Amy Bee"),
                normalLogic.getFilteredPersonList().stream().map(p -> p.getName().fullName).toList());
    }

    @Test
    public void failedSavePreservesSortAndDisplayedList() throws Exception {
        Person mary = client("C3", "Mary O'Connor");
        Person alex = client("C1", "Alex Yeoh");
        ModelManager model = new ModelManager(book(mary, alex), new UserPrefs());
        Path dataPath = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("save failed");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("sort");

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertEquals(SortOrder.ASC, model.getSortOrder());
        assertEquals(List.of(alex, mary), logic.getFilteredPersonList());
        assertEquals(List.of(mary, alex), model.getAddressBook().getPersonList());
        assertFalse(Files.exists(dataPath));
    }

    private static Person client(String reference, String name) {
        int number = Integer.parseInt(reference.substring(1));
        return new PersonBuilder().withClientReference(reference).withName(name)
                .withPhone("900000" + String.format("%02d", number))
                .withEmail(reference.toLowerCase() + "@example.com")
                .withCardDetails("400000000000" + String.format("%04d", number),
                        "123", "12/28", "Visa")
                .build();
    }

    private static AddressBook book(Person... persons) {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(persons));
        return book;
    }

    private static class CountingStorage extends StorageManager {
        private int saveCount;

        CountingStorage(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage) {
            super(addressBookStorage, userPrefsStorage);
        }

        @Override
        public void saveAddressBook(seedu.address.model.ReadOnlyAddressBook addressBook) throws IOException {
            saveCount++;
            super.saveAddressBook(addressBook);
        }
    }
}
