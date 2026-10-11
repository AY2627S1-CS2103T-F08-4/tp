package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim().replaceAll(" +", " ");
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a client reference and normalizes it to uppercase.
     */
    public static ClientReference parseClientReference(String reference) throws ParseException {
        requireNonNull(reference);
        String trimmed = reference.trim();
        if (!ClientReference.isValidClientReference(trimmed)) {
            throw new ParseException(ClientReference.MESSAGE_CONSTRAINTS);
        }
        return new ClientReference(trimmed);
    }

    /** Parses a nonnegative whole-number credit score. */
    public static CreditScore parseCreditScore(String creditScore) throws ParseException {
        requireNonNull(creditScore);
        String trimmed = creditScore.trim();
        if (!CreditScore.isValidCreditScore(trimmed)) {
            throw new ParseException(CreditScore.MESSAGE_CONSTRAINTS);
        }
        return new CreditScore(trimmed);
    }

    /** Parses a nonnegative debt amount with at most two decimal places. */
    public static Debt parseDebt(String debt) throws ParseException {
        requireNonNull(debt);
        String trimmed = debt.trim();
        if (!Debt.isValidDebt(trimmed)) {
            throw new ParseException(Debt.MESSAGE_CONSTRAINTS);
        }
        return new Debt(trimmed);
    }

    /** Parses a card number as plain text. */
    public static String parseCardNumber(String cardNumber) throws ParseException {
        return parseRequiredCardField(cardNumber, CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS);
    }

    /** Parses a CVV as plain text. */
    public static String parseCvv(String cvv) throws ParseException {
        return parseRequiredCardField(cvv, CardDetails.CVV_MESSAGE_CONSTRAINTS);
    }

    /** Parses an expiry date in MM/YY format with a valid month. */
    public static String parseExpiryDate(String expiryDate) throws ParseException {
        requireNonNull(expiryDate);
        String trimmed = expiryDate.trim();
        if (!CardDetails.isValidExpiryDate(trimmed)) {
            throw new ParseException(CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS);
        }
        return trimmed;
    }

    /** Parses a provider name. */
    public static String parseProvider(String provider) throws ParseException {
        return parseRequiredCardField(provider, CardDetails.PROVIDER_MESSAGE_CONSTRAINTS);
    }

    private static String parseRequiredCardField(String value, String message) throws ParseException {
        requireNonNull(value);
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new ParseException(message);
        }
        return trimmed;
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
