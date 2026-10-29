package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Address;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_minimalProfile_omitsMissingContactsAndTheirLabels() {
        Person person = new Person(new ClientReference("C0001"), new Name("John Doe"),
                null, null, null, null, null, null, Collections.emptySet());

        assertEquals("John Doe", Messages.format(person));
    }

    @Test
    public void format_partialContacts_includesOnlySuppliedContacts() {
        Person minimal = new Person(new ClientReference("C0001"), new Name("John Doe"),
                null, null, null, null, null, null, Collections.emptySet());
        Person phoneOnly = new PersonBuilder(minimal).withPhone("85355255").build();
        Person emailOnly = new PersonBuilder(minimal).withEmail("john@example.com").build();
        Person addressOnly = new PersonBuilder(minimal).withAddress("123 Main Street").build();
        Person phoneAndAddress = new PersonBuilder(phoneOnly).withAddress("123 Main Street").withTags("friend").build();

        assertEquals("John Doe; Phone: 85355255", Messages.format(phoneOnly));
        assertEquals("John Doe; Email: john@example.com", Messages.format(emailOnly));
        assertEquals("John Doe; Address: 123 Main Street", Messages.format(addressOnly));
        assertEquals("John Doe; Phone: 85355255; Address: 123 Main Street; Tags: [friend]",
                Messages.format(phoneAndAddress));
    }

    @Test
    public void format_completeProfile_preservesContactText() {
        Person person = new PersonBuilder().withTags("friend").build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Tags: [friend]", Messages.format(person));
    }

    @Test
    public void format_legacyProfile_preservesContactText() {
        Person person = new Person(new Name("Legacy Person"), new Phone("85355255"),
                new Email("legacy@example.com"), new Address("123 Legacy Street"), Collections.emptySet());

        assertEquals("Legacy Person; Phone: 85355255; Email: legacy@example.com; "
                + "Address: 123 Legacy Street", Messages.format(person));
    }
}
