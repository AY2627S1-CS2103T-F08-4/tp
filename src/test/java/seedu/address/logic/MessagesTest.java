package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

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
                + "Address: 123 Legacy Street; Tags: ", Messages.format(person));
    }
}
