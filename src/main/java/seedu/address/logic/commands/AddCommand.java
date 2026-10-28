package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
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

import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientClash;
import seedu.address.model.person.Person;

/**
 * Adds a referenced client with a name and any available optional details to the in-memory address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = "Format: " + COMMAND_WORD + " "
            + PREFIX_REFERENCE + "REFERENCE "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_CARD_NUMBER + "CARD_NUMBER "
            + PREFIX_CVV + "CVV "
            + PREFIX_EXPIRY_DATE + "EXPIRY_DATE "
            + PREFIX_PROVIDER + "PROVIDER] "
            + "[" + PREFIX_CREDIT_SCORE + "CREDIT_SCORE] "
            + "[" + PREFIX_DEBT + "DEBT] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Card details must be supplied together or omitted entirely.\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_REFERENCE + "C0001 " + PREFIX_NAME + "John Doe";

    public static final String MESSAGE_SUCCESS = "Added client: %1$s %2$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "A client with reference %1$s already exists.";

    private final Person toAdd;
    private final boolean clashConfirmed;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        this(person, false);
    }

    /** Creates an add command, optionally bypassing a previously displayed clash warning. */
    public AddCommand(Person person, boolean clashConfirmed) {
        requireNonNull(person);
        if (person.getClientReference().isEmpty()) {
            throw new IllegalArgumentException("A client reference is required");
        }
        toAdd = person;
        this.clashConfirmed = clashConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON,
                    toAdd.getClientReference().orElseThrow()));
        }

        List<ClientClash> clashes = model.findClientClashes(toAdd, null);
        if (!clashConfirmed && !clashes.isEmpty()) {
            if (clashes.stream().anyMatch(ClientClash::isReferenceClash)) {
                throw new CommandException("Reference number " + toAdd.getClientReference()
                        + " already belongs to another client. Reference numbers must be unique.");
            }
            return CommandResult.requestClashConfirmation(clashes, "");
        }

        model.addPerson(toAdd);
        return CommandResult.selectPerson(
                String.format(MESSAGE_SUCCESS, toAdd.getClientReference().orElseThrow(), toAdd.getName()), toAdd);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd)
                && clashConfirmed == otherAddCommand.clashConfirmed;
    }

    @Override
    public int hashCode() {
        return toAdd.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .add("clashConfirmed", clashConfirmed)
                .toString();
    }
}
