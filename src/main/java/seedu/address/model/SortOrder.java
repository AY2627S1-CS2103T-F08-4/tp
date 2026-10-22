package seedu.address.model;

import java.util.Comparator;

import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/** The order used only for displaying clients in the current session. */
public enum SortOrder {
    ORIGINAL,
    ASC,
    DESC;

    /** Returns null for the original order, as required by JavaFX's SortedList. */
    public Comparator<Person> comparator() {
        if (this == ORIGINAL) {
            return null;
        }
        return (first, second) -> {
            int nameComparison = compareNames(first.getName().fullName, second.getName().fullName);
            if (nameComparison != 0) {
                return this == ASC ? nameComparison : -nameComparison;
            }
            return compareReferences(first.getClientReference(), second.getClientReference());
        };
    }

    private static int compareNames(String first, String second) {
        int firstOffset = 0;
        int secondOffset = 0;
        while (firstOffset < first.length() && secondOffset < second.length()) {
            int firstCodePoint = first.codePointAt(firstOffset);
            int secondCodePoint = second.codePointAt(secondOffset);
            int comparison = Integer.compare(Character.toLowerCase(firstCodePoint),
                    Character.toLowerCase(secondCodePoint));
            if (comparison != 0) {
                return comparison;
            }
            firstOffset += Character.charCount(firstCodePoint);
            secondOffset += Character.charCount(secondCodePoint);
        }
        return Integer.compare(first.length() - firstOffset, second.length() - secondOffset);
    }

    private static int compareReferences(ClientReference first, ClientReference second) {
        if (first == null) {
            return second == null ? 0 : 1;
        }
        if (second == null) {
            return -1;
        }
        return first.value.compareTo(second.value);
    }
}
