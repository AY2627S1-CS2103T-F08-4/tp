package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.Storage;

/** Writes the current address book to its configured data file. */
public class AddressBookSync {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";
    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Storage storage;

    public AddressBookSync(Storage storage) {
        this.storage = requireNonNull(storage);
    }

    /** Saves the current address book. Can also be called by a future sync command. */
    public void sync(ReadOnlyAddressBook addressBook) throws CommandException {
        requireNonNull(addressBook);
        try {
            storage.saveAddressBook(addressBook);
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException e) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, e.getMessage()), e);
        }
    }
}
