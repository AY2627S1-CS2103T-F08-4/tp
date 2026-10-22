package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.SortOrder;

/** Changes the displayed order of clients without changing stored data. */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = "Format: sort [asc|desc|reset]";
    public static final String MESSAGE_ASC = "Sorted %d client(s) by name (A-Z).";
    public static final String MESSAGE_DESC = "Sorted %d client(s) by name (Z-A).";
    public static final String MESSAGE_RESET = "Client list restored to original order.";
    public static final String MESSAGE_EMPTY = "No clients to sort. The chosen order will apply to clients "
            + "added later.";
    public static final String MESSAGE_INVALID_ORDER = "Invalid sort order: %s. " + MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_COMMAND = "Invalid sort command. " + MESSAGE_USAGE;

    private final SortOrder sortOrder;

    public SortCommand(SortOrder sortOrder) {
        this.sortOrder = requireNonNull(sortOrder);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setSortOrder(sortOrder);
        if (sortOrder == SortOrder.ORIGINAL) {
            return CommandResult.preserveSelectionOrFirst(MESSAGE_RESET);
        }
        int count = model.getFilteredPersonList().size();
        String message = count == 0 ? MESSAGE_EMPTY
                : String.format(sortOrder == SortOrder.ASC ? MESSAGE_ASC : MESSAGE_DESC, count);
        return CommandResult.preserveSelectionOrFirst(message);
    }

    @Override
    public boolean isDataChanging() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand command && sortOrder == command.sortOrder;
    }

    @Override
    public int hashCode() {
        return sortOrder.hashCode();
    }
}
