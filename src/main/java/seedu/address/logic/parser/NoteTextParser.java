package seedu.address.logic.parser;

import java.util.regex.Pattern;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ServiceNote;

/** Applies the shared escape and validation rules for free-text service notes. */
final class NoteTextParser {
    private static final Pattern UNESCAPED_PREFIX =
            Pattern.compile("(?<=\\p{javaWhitespace})[A-Za-z][A-Za-z0-9]*/");

    private NoteTextParser() { }

    static ServiceNote parse(String raw) throws ParseException {
        String value = raw.strip();
        if (UNESCAPED_PREFIX.matcher(" " + value).find()) {
            throw new ParseException("Prefix-shaped text in a service note must be escaped with \\.");
        }
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\\' && i + 1 < value.length()) {
                if (value.charAt(i + 1) == '\\') {
                    decoded.append('\\');
                    i++;
                    continue;
                }
                if (Character.isLetter(value.charAt(i + 1))) {
                    int slash = value.indexOf('/', i + 1);
                    if (slash > i + 1 && value.substring(i + 1, slash).matches("[A-Za-z][A-Za-z0-9]*")) {
                        decoded.append(value, i + 1, slash + 1);
                        i = slash;
                        continue;
                    }
                }
            }
            decoded.append(current);
        }
        String note = decoded.toString();
        if (note.isEmpty()) {
            return null;
        }
        if (!ServiceNote.isValidServiceNote(note)) {
            throw new ParseException(ServiceNote.MESSAGE_CONSTRAINTS);
        }
        return new ServiceNote(note);
    }
}
