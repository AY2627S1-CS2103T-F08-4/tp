package seedu.address.logic.parser;

import seedu.address.logic.commands.RemoveCardCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the single client reference required by {@code removecard}. */
public class RemoveCardCommandParser implements Parser<RemoveCardCommand> {
    public static final String MESSAGE_MISSING_REFERENCE =
            "Missing client reference. Format: removecard r/REFERENCE";
    public static final String MESSAGE_REPEATED_PARAMETER = "Parameter was specified more than once.";
    public static final String MESSAGE_INVALID_COMMAND = "Invalid removecard command. Format: removecard r/REFERENCE";

    @Override
    public RemoveCardCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty() || trimmed.equals(CliSyntax.PREFIX_REFERENCE.getPrefix())) {
            throw new ParseException(MESSAGE_MISSING_REFERENCE);
        }

        String[] parameters = trimmed.split("\\s+");
        int referenceCount = 0;
        for (String parameter : parameters) {
            if (parameter.startsWith(CliSyntax.PREFIX_REFERENCE.getPrefix())) {
                referenceCount++;
            }
        }
        if (referenceCount > 1) {
            throw new ParseException(MESSAGE_REPEATED_PARAMETER);
        }
        if (parameters.length != 1 || !parameters[0].startsWith(CliSyntax.PREFIX_REFERENCE.getPrefix())) {
            throw new ParseException(MESSAGE_INVALID_COMMAND);
        }

        String referenceValue = parameters[0].substring(CliSyntax.PREFIX_REFERENCE.getPrefix().length());
        return new RemoveCardCommand(ParserUtil.parseClientReference(referenceValue));
    }
}
