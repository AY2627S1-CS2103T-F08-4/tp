package seedu.address.testutil;

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

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_REFERENCE + person.getClientReference().orElseThrow().value + " ");
        sb.append(PREFIX_NAME + person.getName().fullName + " ");
        person.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        person.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        person.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        person.getCardDetails().ifPresent(details -> {
            sb.append(PREFIX_CARD_NUMBER).append(details.getCardNumber()).append(" ");
            sb.append(PREFIX_CVV).append(details.getCvv()).append(" ");
            sb.append(PREFIX_EXPIRY_DATE).append(details.getExpiryDate()).append(" ");
            sb.append(PREFIX_PROVIDER).append(details.getProvider()).append(" ");
        });
        person.getCreditScore().ifPresent(score -> sb.append(PREFIX_CREDIT_SCORE).append(score).append(" "));
        person.getDebt().ifPresent(debt -> sb.append(PREFIX_DEBT).append(debt).append(" "));
        person.getTags().stream().forEach(
            s -> sb.append(PREFIX_TAG + s.tagName + " ")
        );
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getClientReference().ifPresent(reference -> sb.append(PREFIX_REFERENCE)
                .append(reference.value).append(" "));
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        descriptor.getCardNumber().ifPresent(cardNumber -> sb.append(PREFIX_CARD_NUMBER)
                .append(cardNumber).append(" "));
        descriptor.getCvv().ifPresent(cvv -> sb.append(PREFIX_CVV).append(cvv).append(" "));
        descriptor.getExpiryDate().ifPresent(expiryDate -> sb.append(PREFIX_EXPIRY_DATE)
                .append(expiryDate).append(" "));
        descriptor.getProvider().ifPresent(provider -> sb.append(PREFIX_PROVIDER)
                .append(provider).append(" "));
        descriptor.getCreditScore().ifPresent(score -> sb.append(PREFIX_CREDIT_SCORE)
                .append(score).append(" "));
        descriptor.getDebt().ifPresent(debt -> sb.append(PREFIX_DEBT).append(debt).append(" "));
        if (descriptor.getTags().isPresent()) {
            Set<Tag> tags = descriptor.getTags().get();
            if (tags.isEmpty()) {
                sb.append(PREFIX_TAG);
            } else {
                tags.forEach(s -> sb.append(PREFIX_TAG).append(s.tagName).append(" "));
            }
        }
        return sb.toString();
    }
}
