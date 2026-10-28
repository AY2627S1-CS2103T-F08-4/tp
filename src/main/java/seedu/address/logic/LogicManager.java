package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelStage;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String LOAD_FAILURE_MESSAGE = "Unable to load client data. Existing data was not changed. "
            + "Commands that modify client data are disabled for this session.";
    public static final String SAVE_FAILURE_MESSAGE = "Unable to save client data. No changes were made.";
    public static final String NOTE_LOAD_FAILURE_MESSAGE = LOAD_FAILURE_MESSAGE;
    public static final String FILE_OPS_ERROR_FORMAT = AddressBookSync.FILE_OPS_ERROR_FORMAT;

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            AddressBookSync.FILE_OPS_PERMISSION_ERROR_FORMAT;

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final AddressBookSync addressBookSync;
    private final AddressBookParser addressBookParser;
    private final String startupMessage;
    private final boolean isProtectedMode;
    private final Clock clock;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, "", false, Clock.systemUTC());
    }

    /** Creates logic with the result of loading client data at startup. */
    public LogicManager(Model model, Storage storage, String startupMessage, boolean isProtectedMode) {
        this(model, storage, startupMessage, isProtectedMode, Clock.systemUTC());
    }

    /** Creates logic with a clock shared by each staged model. */
    LogicManager(Model model, Storage storage, String startupMessage, boolean isProtectedMode, Clock clock) {
        this.model = model;
        this.addressBookSync = new AddressBookSync(storage);
        addressBookParser = new AddressBookParser();
        this.startupMessage = startupMessage;
        this.isProtectedMode = isProtectedMode;
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        Command command;
        try {
            command = addressBookParser.parseCommand(commandText);
        } catch (ParseException e) {
            logger.info("User command rejected during parsing");
            throw e;
        }
        logger.info("User command: " + command.getClass().getSimpleName());
        if (command.isDataChanging()) {
            if (isProtectedMode) {
                throw new CommandException(LOAD_FAILURE_MESSAGE);
            }
            ModelStage stage = model.beginStaging(clock);
            Model stagedModel = stage.model();
            CommandResult result = command.execute(stagedModel);
            try {
                addressBookSync.sync(stagedModel.getAddressBook());
            } catch (CommandException e) {
                throw new CommandException(SAVE_FAILURE_MESSAGE, e);
            }
            stage.commit();
            stage.timestampAction().ifPresent(action -> logger.info("Timestamp " + action + " recorded"));
            return result;
        }
        return command.execute(model);
    }

    @Override
    public String getStartupMessage() {
        return startupMessage;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
