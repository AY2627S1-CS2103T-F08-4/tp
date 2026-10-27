package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientReference;

/** Parses the two single-valued parameters of the note command. */
public class NoteCommandParser implements Parser<NoteCommand> {
    private static final Pattern PREFIX = Pattern.compile("(?<=\\p{javaWhitespace})([A-Za-z][A-Za-z0-9]*/)");

    @Override
    public NoteCommand parse(String args) throws ParseException {
        Matcher matcher = PREFIX.matcher(args);
        List<Parameter> parameters = new ArrayList<>();
        while (matcher.find()) {
            parameters.add(new Parameter(matcher.group(1), matcher.start(1), matcher.end(1)));
        }

        Parameter referenceParameter = parameters.stream()
                .filter(parameter -> parameter.prefix().equals("r/"))
                .findFirst().orElse(null);
        if (referenceParameter == null) {
            throw new ParseException(NoteCommand.MESSAGE_MISSING_REFERENCE);
        }

        String referenceValue = valueOf(args, parameters, referenceParameter).strip();
        if (referenceValue.isEmpty()) {
            throw new ParseException(NoteCommand.MESSAGE_MISSING_REFERENCE);
        }
        String[] referenceParts = referenceValue.split("\\p{javaWhitespace}+", 2);
        ClientReference reference = ParserUtil.parseClientReference(referenceParts[0]);

        boolean hasPreamble = !args.substring(0, parameters.getFirst().start()).isBlank();
        boolean hasUnknownPrefix = parameters.stream()
                .anyMatch(parameter -> !parameter.prefix().equals("r/") && !parameter.prefix().equals("s/"));
        if (hasPreamble || hasUnknownPrefix || referenceParts.length > 1) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_FORMAT);
        }

        for (String prefix : List.of("r/", "s/")) {
            long count = parameters.stream().filter(parameter -> parameter.prefix().equals(prefix)).count();
            if (count > 1) {
                throw new ParseException(String.format(NoteCommand.MESSAGE_REPEATED_PREFIX, prefix));
            }
        }

        Parameter noteParameter = parameters.stream()
                .filter(parameter -> parameter.prefix().equals("s/"))
                .findFirst().orElse(null);
        if (noteParameter == null) {
            throw new ParseException(NoteCommand.MESSAGE_MISSING_NOTE);
        }
        return new NoteCommand(reference, NoteTextParser.parse(valueOf(args, parameters, noteParameter)));
    }

    private static String valueOf(String args, List<Parameter> parameters, Parameter parameter) {
        int index = parameters.indexOf(parameter);
        int end = index + 1 < parameters.size() ? parameters.get(index + 1).start() : args.length();
        return args.substring(parameter.end(), end);
    }

    private record Parameter(String prefix, int start, int end) { }
}
