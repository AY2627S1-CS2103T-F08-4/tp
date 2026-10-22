package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SortOrderTest {

    @Test
    public void sorting_ordersNamesAndReferencesWithoutChangingStoredOrder() {
        Person accented = client("C4", "Émile");
        Person anna = client("C5", "Anna Lee");
        Person alexLaterReference = client("C9", "alex Yeoh");
        Person ann = client("C6", "Ann Lee");
        Person alexEarlierReference = client("C1", "Alex Yeoh");
        List<Person> stored = List.of(accented, anna, alexLaterReference, ann, alexEarlierReference);
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(stored);
        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        model.setSortOrder(SortOrder.ASC);
        assertEquals(List.of(alexEarlierReference, alexLaterReference, ann, anna, accented),
                model.getFilteredPersonList());

        model.setSortOrder(SortOrder.DESC);
        assertEquals(List.of(accented, anna, ann, alexEarlierReference, alexLaterReference),
                model.getFilteredPersonList());

        model.setSortOrder(SortOrder.ORIGINAL);
        assertEquals(stored, model.getFilteredPersonList());
        assertEquals(stored, model.getAddressBook().getPersonList());
    }

    @Test
    public void sorting_legacyClientWithoutReferenceUsesStoredOrderForFinalTie() {
        Person legacy = new PersonBuilder().withName("Alex Yeoh").build();
        Person referenced = client("C1", "alex Yeoh");
        AddressBook addressBook = new AddressBook();
        addressBook.setPersons(List.of(legacy, referenced));
        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        model.setSortOrder(SortOrder.ASC);
        assertEquals(List.of(referenced, legacy), model.getFilteredPersonList());
        model.setSortOrder(SortOrder.DESC);
        assertEquals(List.of(referenced, legacy), model.getFilteredPersonList());
    }

    @Test
    public void sorting_comparesSpaceApostropheAndHyphenByCodePoint() {
        Person hyphen = client("C3", "Ann-Lee");
        Person letters = client("C4", "Anna Lee");
        Person apostrophe = client("C2", "Ann'Lee");
        Person space = client("C1", "Ann Lee");
        ModelManager model = new ModelManager(book(hyphen, letters, apostrophe, space), new UserPrefs());

        model.setSortOrder(SortOrder.ASC);
        assertEquals(List.of(space, apostrophe, hyphen, letters), model.getFilteredPersonList());
    }

    private static AddressBook book(Person... persons) {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(persons));
        return book;
    }

    private static Person client(String reference, String name) {
        return new PersonBuilder().withClientReference(reference).withName(name).build();
    }
}
