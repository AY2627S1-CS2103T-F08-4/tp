package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;

/**
 * Verifies references, validation, and existing client details through the in-memory model.
 */
public class AddCommandIntegrationTest {
    private static final String ADD_CLIENT = "add r/C0001 n/Alex Yeoh p/87438807"
            + " e/alex@example.com a/10 Main Street cn/4111111111111111 cvv/123 exp/12/28"
            + " provider/Visa cs/700 d/1250.50 t/friends";
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_validClient_preservesContactCardFinancialAndTagDetails() throws Exception {
        CommandResult result = parser.parseCommand("  " + ADD_CLIENT + "  ").execute(model);
        assertEquals("Added client: C0001 Alex Yeoh", result.getFeedbackToUser());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        Person client = model.getAddressBook().getPersonList().getFirst();
        assertEquals("C0001", client.getClientReference().value);
        assertEquals("Alex Yeoh", client.getName().fullName);
        assertEquals("87438807", client.getPhone().value);
        assertEquals("alex@example.com", client.getEmail().value);
        assertEquals("10 Main Street", client.getAddress().value);
        assertEquals("4111111111111111", client.getCardDetails().getCardNumber());
        assertEquals("123", client.getCardDetails().getCvv());
        assertEquals("12/28", client.getCardDetails().getExpiryDate());
        assertEquals("Visa", client.getCardDetails().getProvider());
        assertEquals("700", client.getCreditScore().toString());
        assertEquals("1250.50", client.getDebt().toString());
        assertEquals(1, client.getTags().size());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void execute_sharedNameAndContactDetails_distinctReferencesAccepted() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        parser.parseCommand(ADD_CLIENT.replace("C0001", "C0002")).execute(model);
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
        assertEquals(before.getClientReference(), after.getClientReference());
        assertEquals(before.getCardDetails(), after.getCardDetails());
        assertEquals(before.getCreditScore(), after.getCreditScore());
        assertEquals(before.getDebt(), after.getDebt());
        assertEquals(before.getTags(), after.getTags());
        assertEquals("91234567", after.getPhone().value);
    }

    @Test
    public void execute_invalidCommand_leavesMemoryUnchanged() throws Exception {
        parser.parseCommand(ADD_CLIENT).execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[] {ADD_CLIENT.replace("C0001", "C0002").replace("87438807", "12"),
            ADD_CLIENT.replace("cs/700", "cs/-1"), ADD_CLIENT + " r/C0002", "add r/C0002 n/Alex Yeoh"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command).execute(model));
            assertEquals(before, model.getAddressBook());
        }
    }
}
