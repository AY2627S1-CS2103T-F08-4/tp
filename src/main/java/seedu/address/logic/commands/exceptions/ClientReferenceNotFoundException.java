package seedu.address.logic.commands.exceptions;

import seedu.address.model.person.ClientReference;

/**
 * Signals that no person exists with the specified client reference.
 */
public class ClientReferenceNotFoundException extends CommandException {

    public ClientReferenceNotFoundException(ClientReference clientReference) {
        super("No person found with client reference: " + clientReference);
    }
}
