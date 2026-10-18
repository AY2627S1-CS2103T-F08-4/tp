package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

class PersonCardTest {

    @Test
    void formatCardDetails_completeCardDetails_masksCardNumberAndOmitsCvv() {
        var person = new PersonBuilder()
                .withCardDetails("1234567890123456", "123", "12/28", "Visa")
                .build();

        assertEquals("Card: Visa ****3456 exp 12/28", PersonCard.formatCardDetails(person));
    }

    @Test
    void formatCardDetails_legacyPerson_returnsEmptyText() {
        var person = new Person(
                new Name("Legacy Person"),
                new Phone("85355255"),
                new Email("legacy@example.com"),
                new Address("123 Legacy Street"),
                Collections.emptySet());

        assertEquals("", PersonCard.formatCardDetails(person));
    }

    @Test
    void formatCardDetails_referencedPersonWithoutCard_returnsEmptyText() {
        var withCard = new PersonBuilder().withClientReference("C0001").build();
        var withoutCard = new Person(withCard.getClientReference(), withCard.getName(), withCard.getPhone(),
                withCard.getEmail(), withCard.getAddress(), null, withCard.getCreditScore(),
                withCard.getDebt(), withCard.getTags());

        assertEquals("", PersonCard.formatCardDetails(withoutCard));
    }
}
