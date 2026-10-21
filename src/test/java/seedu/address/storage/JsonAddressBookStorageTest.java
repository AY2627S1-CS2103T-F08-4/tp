package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_referencedClientsWithSameName_preservesReferences() throws Exception {
        Path filePath = testFolder.resolve("ReferencedAddressBook.json");
        AddressBook original = new AddressBook();
        Person first = new PersonBuilder().withName("Alex Yeoh").withClientReference("C0001").build();
        Person second = new PersonBuilder().withName("Alex Yeoh").withClientReference("C0002").build();
        original.addPerson(first);
        original.addPerson(second);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);

        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_minimalProfile_omitsMissingDetailsAndPreservesAbsence() throws Exception {
        Path filePath = testFolder.resolve("minimal.json");
        Person minimal = new Person(new ClientReference("C0001"), new Name("John Doe"),
                null, null, null, null, null, null, Collections.emptySet());
        AddressBook original = new AddressBook();
        original.addPerson(minimal);

        new JsonAddressBookStorage(filePath).saveAddressBook(original);

        JsonNode expectedJson = JsonUtil.fromJsonString(
                "{\"persons\":[{\"clientReference\":\"C0001\",\"name\":\"John Doe\",\"tags\":[]}]}", JsonNode.class);
        assertEquals(expectedJson, JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class));
        assertEquals(original, new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_independentDetails_preservesEveryCombination() throws Exception {
        Path filePath = testFolder.resolve("optional-details.json");
        AddressBook original = new AddressBook();
        Instant created = Instant.parse("2026-03-15T01:42:00Z");
        Instant modified = Instant.parse("2026-10-12T06:18:00Z");
        Set<Tag> tags = Set.of(new Tag("friend"));
        for (int mask = 0; mask < 64; mask++) {
            Phone phone = (mask & 1) == 0 ? null : new Phone(PersonBuilder.DEFAULT_PHONE);
            Email email = (mask & 2) == 0 ? null : new Email(PersonBuilder.DEFAULT_EMAIL);
            Address address = (mask & 4) == 0 ? null : new Address(PersonBuilder.DEFAULT_ADDRESS);
            CardDetails card = (mask & 8) == 0 ? null : new CardDetails(PersonBuilder.DEFAULT_CARD_NUMBER,
                    PersonBuilder.DEFAULT_CVV, PersonBuilder.DEFAULT_EXPIRY_DATE, PersonBuilder.DEFAULT_PROVIDER);
            CreditScore score = (mask & 16) == 0 ? null : new CreditScore("0");
            Debt debt = (mask & 32) == 0 ? null : new Debt("0.00");
            original.addPerson(new Person(new ClientReference("C" + mask), new Name("John Doe"),
                    phone, email, address, card, score, debt, tags).withTimestamps(created, modified));
            String legacyName = "Legacy Client " + (char) ('A' + mask / 26) + (char) ('A' + mask % 26);
            original.addPerson(new Person(new Name(legacyName),
                    phone, email, address, card, score, debt, tags).withTimestamps(null, modified));
        }

        new JsonAddressBookStorage(filePath).saveAddressBook(original);

        assertEquals(original, new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow());
        JsonNode persons = JsonUtil.fromJsonString(Files.readString(filePath), JsonNode.class).get("persons");
        for (int index = 0; index < original.getPersonList().size(); index++) {
            Person person = original.getPersonList().get(index);
            JsonNode json = persons.get(index);
            assertEquals(person.getPhone().isPresent(), json.has("phone"));
            assertEquals(person.getEmail().isPresent(), json.has("email"));
            assertEquals(person.getAddress().isPresent(), json.has("address"));
            assertEquals(person.getCardDetails().isPresent(), json.has("cardDetails"));
            assertEquals(person.getCreditScore().isPresent(), json.has("creditScore"));
            assertEquals(person.getDebt().isPresent(), json.has("debt"));
        }
    }

    @Test
    public void readAddressBook_invalidIncompleteProfiles_throwsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("invalid-incomplete.json");
        List<String> invalidPersons = List.of(
                "{\"clientReference\":\"C0001\"}",
                "{\"clientReference\":\"C0001\",\"name\":null}",
                "{\"clientReference\":\"C0001\",\"name\":\" \"}",
                "{\"clientReference\":\"bad reference\",\"name\":\"John Doe\"}",
                "{\"clientReference\":\"C0001\",\"name\":\"John Doe\",\"phone\":\"\"}",
                "{\"clientReference\":\"C0001\",\"name\":\"John Doe\",\"creditScore\":\"-1\"}",
                "{\"clientReference\":\"C0001\",\"name\":\"John Doe\",\"cardDetails\":{}}");

        for (String person : invalidPersons) {
            String json = "{\"persons\":[" + person + "]}";
            Files.writeString(filePath, json);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, Files.readString(filePath));
        }
    }

    @Test
    public void readAddressBook_duplicateMinimalReferences_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("duplicate-minimal.json");
        Files.writeString(filePath, """
                {"persons":[{"clientReference":"C0001","name":"John Doe"},
                            {"clientReference":"c0001","name":"Jane Doe"}]}
                """);

        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    @Test
    public void saveAddressBook_replacementFails_keepsExistingTarget() throws Exception {
        Path target = testFolder.resolve("existing.json");
        Files.createDirectory(target);
        Path sentinel = target.resolve("sentinel.txt");
        Files.writeString(sentinel, "keep this");

        assertThrows(IOException.class, () -> new JsonAddressBookStorage(target).saveAddressBook(new AddressBook()));
        assertTrue(Files.isDirectory(target));
        assertEquals("keep this", Files.readString(sentinel));
        try (var files = Files.list(testFolder)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
