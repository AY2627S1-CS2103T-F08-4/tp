package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Verifies references, validation, and existing client details through the in-memory model.
 */
public class AddCommandIntegrationTest {
    private static final Instant CREATED = Instant.parse("2026-03-15T01:42:00Z");
    private static final String ADD_CLIENT = "add r/C0001 n/Alex Yeoh p/87438807"
            + " e/alex@example.com a/10 Main Street cn/4111111111111111 cvv/123 exp/12/28"
            + " provider/Visa cs/700 d/1250.50 t/friends";
    private final ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(),
            Clock.fixed(CREATED, ZoneOffset.UTC));
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_minimalClient_createsProfileWithoutFabricatedDetails() throws Exception {
        CommandResult result = parser.parseCommand("add r/c0002 n/Alex Yeoh").execute(model);
        Person expected = new Person(new ClientReference("C0002"), new Name("Alex Yeoh"),
                null, null, null, null, null, null, Collections.emptySet()).withTimestamps(CREATED, CREATED);

        assertEquals("Added client: C0002 Alex Yeoh", result.getFeedbackToUser());
        assertEquals(expected, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_duplicateMinimalReference_rejectedWithoutChangingModel() throws Exception {
        parser.parseCommand("add r/C0001 n/John Doe").execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, "A client with reference C0001 already exists.", ()
                -> parser.parseCommand("add r/c0001 n/Jane Doe e/jane@example.com").execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_validClient_preservesContactCardFinancialAndTagDetails() throws Exception {
        CommandResult result = parser.parseCommand("  " + ADD_CLIENT + "  ").execute(model);
        assertEquals("Added client: C0001 Alex Yeoh", result.getFeedbackToUser());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        Person client = model.getAddressBook().getPersonList().getFirst();
        assertEquals("C0001", client.getClientReference().orElseThrow().value);
        assertEquals("Alex Yeoh", client.getName().fullName);
        assertEquals("87438807", client.getPhone().orElseThrow().value);
        assertEquals("alex@example.com", client.getEmail().orElseThrow().value);
        assertEquals("10 Main Street", client.getAddress().orElseThrow().value);
        assertEquals("4111111111111111", client.getCardDetails().orElseThrow().getCardNumber());
        assertEquals("123", client.getCardDetails().orElseThrow().getCvv());
        assertEquals("12/28", client.getCardDetails().orElseThrow().getExpiryDate());
        assertEquals("Visa", client.getCardDetails().orElseThrow().getProvider());
        assertEquals("700", client.getCreditScore().orElseThrow().toString());
        assertEquals("1250.50", client.getDebt().orElseThrow().toString());
        assertEquals(1, client.getTags().size());
        assertFalse(result.shouldShowHelp());
        assertFalse(result.shouldExit());
    }

    @Test
    public void execute_sharedNameAndContactDetails_distinctReferencesAccepted() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        parser.parseCommand(ADD_CLIENT.replace("C0001", "C0002")
                .replace("87438807", "87438808")
                .replace("alex@example.com", "alex2@example.com")
                .replace("4111111111111111", "5555555555554444")).execute(model);
        assertEquals(2, model.getAddressBook().getPersonList().size());
        Person first = model.getAddressBook().getPersonList().get(0);
        Person second = model.getAddressBook().getPersonList().get(1);
        assertFalse(first.isSamePerson(second));
        assertFalse(first.equals(second));
    }

    @Test
    public void execute_duplicateReference_differentDetailsStillRejectedAtomically() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        Command duplicate = parser.parseCommand(ADD_CLIENT.replace("C0001", "c0001")
                .replace("Alex Yeoh", "Mary O'Connor").replace("87438807", "91234567")
                .replace("alex@example.com", "mary@example.com").replace("10 Main Street", "20 Other Road"));
        assertThrows(CommandException.class, "A client with reference C0001 already exists.", ()
                -> duplicate.execute(model));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void execute_editContactDetails_preservesReferenceAndFinancialDetails() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        Person before = model.getAddressBook().getPersonList().getFirst();
        parser.parseCommand("edit 1 p/91234567").execute(model);
        Person after = model.getAddressBook().getPersonList().getFirst();
        assertEquals(before.getClientReference().orElse(null), after.getClientReference().orElse(null));
        assertEquals(before.getCardDetails(), after.getCardDetails());
        assertEquals(before.getCreditScore(), after.getCreditScore());
        assertEquals(before.getDebt(), after.getDebt());
        assertEquals(before.getTags(), after.getTags());
        assertEquals("91234567", after.getPhone().orElseThrow().value);
    }

    @Test
    public void execute_invalidCommand_leavesMemoryUnchanged() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[] {ADD_CLIENT.replace("C0001", "C0002").replace("87438807", "12"),
            ADD_CLIENT.replace("cs/700", "cs/-1"), ADD_CLIENT + " r/C0002", "add r/C0002 n/Alex Yeoh p/"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command).execute(model));
            assertEquals(before, model.getAddressBook());
        }
    }
}
