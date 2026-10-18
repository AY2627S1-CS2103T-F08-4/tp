package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches any keyword within the name field and any full number within the phone field.
 * When both fields are supplied, both must match. An empty search matches nobody.
 */
public class FindPersonPredicate implements Predicate<Person> {
    private final List<String> nameKeywords;
    private final List<Phone> phoneNumbers;
    private final NameContainsKeywordsPredicate namePredicate;

    /**
     * Creates a search predicate. An empty list omits the corresponding field from the search.
     */
    public FindPersonPredicate(List<String> nameKeywords, List<Phone> phoneNumbers) {
        this.nameKeywords = List.copyOf(nameKeywords);
        this.phoneNumbers = List.copyOf(phoneNumbers);
        namePredicate = new NameContainsKeywordsPredicate(this.nameKeywords);
    }

    @Override
    public boolean test(Person person) {
        if (nameKeywords.isEmpty() && phoneNumbers.isEmpty()) {
            return false;
        }
        return (nameKeywords.isEmpty() || namePredicate.test(person))
                && (phoneNumbers.isEmpty() || phoneNumbers.contains(person.getPhone()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FindPersonPredicate otherPredicate)) {
            return false;
        }
        return nameKeywords.equals(otherPredicate.nameKeywords) && phoneNumbers.equals(otherPredicate.phoneNumbers);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("nameKeywords", nameKeywords).add("phoneNumbers", phoneNumbers).toString();
    }
}
