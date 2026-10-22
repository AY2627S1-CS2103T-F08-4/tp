package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    /** Selection action requested after a successful command refreshes the displayed list. */
    public enum SelectionRequest {
        PRESERVE_OR_FIRST, SELECT_PERSON, SELECT_REFERENCE
    }

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;
    private final ClientReference selectedClientReference;
    private final SelectionRequest selectionRequest;
    private final Person personToSelect;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, SelectionRequest.PRESERVE_OR_FIRST, null, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit,
            SelectionRequest selectionRequest, Person personToSelect, ClientReference selectedClientReference) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.selectedClientReference = selectedClientReference;
        this.selectionRequest = requireNonNull(selectionRequest);
        this.personToSelect = personToSelect;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /** Requests that the UI select a client after this command succeeds. */
    public CommandResult(String feedbackToUser, ClientReference selectedClientReference) {
        this(feedbackToUser, false, false, SelectionRequest.SELECT_REFERENCE,
                null, requireNonNull(selectedClientReference));
    }

    /** Returns a result that keeps the selected client, or selects the first displayed client. */
    public static CommandResult preserveSelectionOrFirst(String feedbackToUser) {
        return new CommandResult(feedbackToUser);
    }

    /** Returns a result that selects a specific client after the list refreshes. */
    public static CommandResult selectPerson(String feedbackToUser, Person person) {
        return new CommandResult(feedbackToUser, false, false, SelectionRequest.SELECT_PERSON,
                requireNonNull(person), null);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public Optional<ClientReference> getSelectedClientReference() {
        return Optional.ofNullable(selectedClientReference);
    }

    public SelectionRequest getSelectionRequest() {
        return selectionRequest;
    }

    public Person getPersonToSelect() {
        return personToSelect;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && selectionRequest == otherCommandResult.selectionRequest
                && Objects.equals(personToSelect, otherCommandResult.personToSelect)
                && Objects.equals(selectedClientReference, otherCommandResult.selectedClientReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, selectedClientReference, selectionRequest, personToSelect);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("selectionRequest", selectionRequest)
                .add("personToSelect", personToSelect);
        if (selectedClientReference != null) {
            builder.add("selectedClientReference", selectedClientReference);
        }
        return builder.toString();
    }

}
