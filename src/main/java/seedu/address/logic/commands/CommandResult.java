package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.ClientClash;
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
    private final boolean shouldShowHelp;

    /** The application should exit. */
    private final boolean shouldExit;
    private final ClientReference selectedClientReference;
    private final SelectionRequest selectionRequest;
    private final Person personToSelect;
    private final List<ClientClash> clientClashes;
    private final String commandToConfirm;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit) {
        this(feedbackToUser, shouldShowHelp, shouldExit, SelectionRequest.PRESERVE_OR_FIRST,
                null, null, List.of(), null);
    }

    private CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit,
            SelectionRequest selectionRequest,
            Person personToSelect,
            ClientReference selectedClientReference,
            List<ClientClash> clientClashes,
            String commandToConfirm) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.shouldShowHelp = shouldShowHelp;
        this.shouldExit = shouldExit;
        this.selectedClientReference = selectedClientReference;
        this.selectionRequest = requireNonNull(selectionRequest);
        this.personToSelect = personToSelect;
        this.clientClashes = List.copyOf(clientClashes);
        this.commandToConfirm = commandToConfirm;
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
                null, requireNonNull(selectedClientReference), List.of(), null);
    }

    /** Returns a result that keeps the selected client, or selects the first displayed client. */
    public static CommandResult preserveSelectionOrFirst(String feedbackToUser) {
        return new CommandResult(feedbackToUser);
    }

    /** Returns a result that selects a specific client after the list refreshes. */
    public static CommandResult selectPerson(String feedbackToUser, Person person) {
        return new CommandResult(feedbackToUser, false, false, SelectionRequest.SELECT_PERSON,
                requireNonNull(person), null, List.of(), null);
    }

    /**
     * Creates a result that asks the UI to obtain confirmation before the
     * original command is executed again.
     */
    public static CommandResult requestClashConfirmation(
            List<ClientClash> clashes,
            String commandToConfirm) {

        requireNonNull(clashes);
        requireNonNull(commandToConfirm);

        String clashMessage = clashes.stream()
                .map(ClientClash::formatMessage)
                .collect(Collectors.joining(System.lineSeparator()));

        return new CommandResult(
                clashMessage + System.lineSeparator()
                        + "Proceed with this operation? Choose Yes or No.",
                false,
                false,
                SelectionRequest.PRESERVE_OR_FIRST,
                null,
                null,
                clashes,
                commandToConfirm);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean shouldShowHelp() {
        return shouldShowHelp;
    }

    public boolean shouldExit() {
        return shouldExit;
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

    /** Returns true when the UI must ask the user before proceeding. */
    public boolean requiresClashConfirmation() {
        return !clientClashes.isEmpty();
    }

    /** Returns the clashes that caused this confirmation request. */
    public List<ClientClash> getClientClashes() {
        return clientClashes;
    }

    /** Returns the original command that should be retried after confirmation. */
    public String getCommandToConfirm() {
        return commandToConfirm;
    }

    /**
     * Returns a copy of this result associated with the original command that
     * requires confirmation.
     */
    public CommandResult withCommandToConfirm(String commandText) {
        requireNonNull(commandText);

        return new CommandResult(
                feedbackToUser,
                shouldShowHelp,
                shouldExit,
                selectionRequest,
                personToSelect,
                selectedClientReference,
                clientClashes,
                commandText);
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
                && shouldShowHelp == otherCommandResult.shouldShowHelp
                && shouldExit == otherCommandResult.shouldExit
                && selectionRequest == otherCommandResult.selectionRequest
                && Objects.equals(personToSelect, otherCommandResult.personToSelect)
                && Objects.equals(selectedClientReference, otherCommandResult.selectedClientReference)
                && Objects.equals(clientClashes, otherCommandResult.clientClashes)
                && Objects.equals(commandToConfirm, otherCommandResult.commandToConfirm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, shouldShowHelp, shouldExit, selectedClientReference,
                selectionRequest, personToSelect, clientClashes, commandToConfirm);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("shouldShowHelp", shouldShowHelp)
                .add("shouldExit", shouldExit)
                .add("selectionRequest", selectionRequest)
                .add("personToSelect", personToSelect)
                .add("clientClashes", clientClashes)
                .add("commandToConfirm", commandToConfirm);
        if (selectedClientReference != null) {
            builder.add("selectedClientReference", selectedClientReference);
        }
        return builder.toString();
    }

}
