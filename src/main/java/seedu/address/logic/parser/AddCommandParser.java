package seedu.address.logic.parser;

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
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final String MESSAGE_INCOMPLETE_CARD_DETAILS = "Card details require all four fields: "
            + PREFIX_CARD_NUMBER + "CARD_NUMBER " + PREFIX_CVV + "CVV "
            + PREFIX_EXPIRY_DATE + "EXPIRY_DATE " + PREFIX_PROVIDER + "PROVIDER.";

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                        PREFIX_CREDIT_SCORE, PREFIX_DEBT, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_REFERENCE, PREFIX_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                PREFIX_CREDIT_SCORE, PREFIX_DEBT);
        ClientReference reference =
                ParserUtil.parseClientReference(argMultimap.getValue(PREFIX_REFERENCE).orElseThrow());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).orElseThrow());
        String phoneValue = argMultimap.getValue(PREFIX_PHONE).orElse(null);
        Phone phone = phoneValue == null ? null : ParserUtil.parsePhone(phoneValue);
        String emailValue = argMultimap.getValue(PREFIX_EMAIL).orElse(null);
        Email email = emailValue == null ? null : ParserUtil.parseEmail(emailValue);
        String addressValue = argMultimap.getValue(PREFIX_ADDRESS).orElse(null);
        Address address = addressValue == null ? null : ParserUtil.parseAddress(addressValue);
        CardDetails cardDetails = parseCardDetails(argMultimap);
        String creditScoreValue = argMultimap.getValue(PREFIX_CREDIT_SCORE).orElse(null);
        CreditScore creditScore = creditScoreValue == null ? null : ParserUtil.parseCreditScore(creditScoreValue);
        String debtValue = argMultimap.getValue(PREFIX_DEBT).orElse(null);
        Debt debt = debtValue == null ? null : ParserUtil.parseDebt(debtValue);
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(reference, name, phone, email, address, cardDetails, creditScore, debt, tagList);

        return new AddCommand(person);
    }

    /** Parses a complete card group, or returns null when none of its prefixes are supplied. */
    private static CardDetails parseCardDetails(ArgumentMultimap argMultimap) throws ParseException {
        Prefix[] cardPrefixes = {PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER};
        boolean hasCardPrefix = Stream.of(cardPrefixes).anyMatch(prefix -> argMultimap.getValue(prefix).isPresent());
        if (!hasCardPrefix) {
            return null;
        }
        if (!arePrefixesPresent(argMultimap, cardPrefixes)) {
            throw new ParseException(MESSAGE_INCOMPLETE_CARD_DETAILS);
        }
        return new CardDetails(
                ParserUtil.parseCardNumber(argMultimap.getValue(PREFIX_CARD_NUMBER).orElseThrow()),
                ParserUtil.parseCvv(argMultimap.getValue(PREFIX_CVV).orElseThrow()),
                ParserUtil.parseExpiryDate(argMultimap.getValue(PREFIX_EXPIRY_DATE).orElseThrow()),
                ParserUtil.parseProvider(argMultimap.getValue(PREFIX_PROVIDER).orElseThrow()));
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
