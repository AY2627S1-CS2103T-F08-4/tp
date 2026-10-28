package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CREDIT_SCORE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CVV;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPIRY_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROVIDER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REFERENCE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SERVICE_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        boolean clashConfirmed = args.endsWith(AddCommandParser.CONFIRM_CLASH_FLAG);
        if (clashConfirmed) {
            args = args.substring(0, args.length() - AddCommandParser.CONFIRM_CLASH_FLAG.length()).trim();
        }
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                        PREFIX_CREDIT_SCORE, PREFIX_DEBT, PREFIX_SERVICE_NOTE, PREFIX_TAG);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                PREFIX_CREDIT_SCORE, PREFIX_DEBT, PREFIX_SERVICE_NOTE);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_REFERENCE).isPresent()) {
            editPersonDescriptor.setClientReference(ParserUtil.parseClientReference(
                    argMultimap.getValue(PREFIX_REFERENCE).get()));
        }

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (argMultimap.getValue(PREFIX_CARD_NUMBER).isPresent()) {
            editPersonDescriptor.setCardNumber(ParserUtil.parseCardNumber(
                    argMultimap.getValue(PREFIX_CARD_NUMBER).get()));
        }
        if (argMultimap.getValue(PREFIX_CVV).isPresent()) {
            editPersonDescriptor.setCvv(ParserUtil.parseCvv(argMultimap.getValue(PREFIX_CVV).get()));
        }
        if (argMultimap.getValue(PREFIX_EXPIRY_DATE).isPresent()) {
            editPersonDescriptor.setExpiryDate(ParserUtil.parseExpiryDate(
                    argMultimap.getValue(PREFIX_EXPIRY_DATE).get()));
        }
        if (argMultimap.getValue(PREFIX_PROVIDER).isPresent()) {
            editPersonDescriptor.setProvider(ParserUtil.parseProvider(
                    argMultimap.getValue(PREFIX_PROVIDER).get()));
        }
        if (argMultimap.getValue(PREFIX_CREDIT_SCORE).isPresent()) {
            editPersonDescriptor.setCreditScore(ParserUtil.parseCreditScore(
                    argMultimap.getValue(PREFIX_CREDIT_SCORE).get()));
        }
        if (argMultimap.getValue(PREFIX_DEBT).isPresent()) {
            editPersonDescriptor.setDebt(ParserUtil.parseDebt(argMultimap.getValue(PREFIX_DEBT).get()));
        }
        if (argMultimap.getValue(PREFIX_SERVICE_NOTE).isPresent()) {
            editPersonDescriptor.setServiceNote(NoteTextParser.parse(argMultimap.getValue(PREFIX_SERVICE_NOTE).get()));
        }
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor, clashConfirmed);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null : "The tokenizer must supply a tag collection";

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
