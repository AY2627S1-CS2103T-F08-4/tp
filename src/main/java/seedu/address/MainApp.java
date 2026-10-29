package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import seedu.address.commons.core.AppPaths;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.AddressBookFileLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.3";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = AppPaths.resolve("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = AppPaths.resolve("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private AddressBookFileLock addressBookLock;
    private String startupError;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        isStorageInitialized(USER_PREFS_FILE_PATH, ADDRESS_BOOK_FILE_PATH);
    }

    /**
     * Acquires exclusive access and initializes storage before reading data or preferences.
     *
     * @return true if initialization succeeds, or false if startup is blocked
     */
    boolean isStorageInitialized(Path prefsPath, Path dataPath) {
        try {
            Optional<AddressBookFileLock> acquired = AddressBookFileLock.tryAcquire(dataPath);
            if (acquired.isEmpty()) {
                startupError = "The client data is already open in another TellerFlow instance. "
                        + "Close that instance and try again.";
                return false;
            }
            addressBookLock = acquired.get();
        } catch (IOException e) {
            logger.warning("Could not obtain exclusive access to client data.");
            startupError = "TellerFlow could not secure access to the client data. "
                    + "Check that its data folder is writable and try again.";
            return false;
        }

        try {
            initializeApplication(prefsPath, dataPath);
            return true;
        } catch (RuntimeException e) {
            releaseAddressBookLock();
            throw e;
        }
    }

    private void initializeApplication(Path prefsPath, Path dataPath) {
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(prefsPath);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataPath);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        InitialData initialData = initModelManager(storage, userPrefs);
        model = initialData.model();

        logic = new LogicManager(model, storage, initialData.message(), initialData.protectedMode());

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /**
     * Loads client data and records the startup message and whether changes must be blocked.
     */
    InitialData initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        try {
            Optional<ReadOnlyAddressBook> savedData = storage.readAddressBook();
            if (savedData.isEmpty()) {
                logger.info("No saved client data found. Showing sample clients.");
                return new InitialData(new ModelManager(SampleDataUtil.getSampleAddressBook(), userPrefs),
                        "No saved client data found. Showing sample clients.", false);
            }
            if (savedData.get().getPersonList().isEmpty()) {
                return new InitialData(new ModelManager(new AddressBook(), userPrefs),
                        "No saved client data found.", false);
            }
            int count = savedData.get().getPersonList().size();
            return new InitialData(new ModelManager(savedData.get(), userPrefs),
                    String.format("Loaded %d client(s).", count), false);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty AddressBook.");
            return new InitialData(new ModelManager(new AddressBook(), userPrefs),
                    LogicManager.LOAD_FAILURE_MESSAGE, true);
        }
    }

    record InitialData(Model model, String message, boolean protectedMode) { }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        if (startupError != null) {
            // Startup can fail before the primary stage has a scene, so use an unowned dialog.
            Alert alert = new Alert(Alert.AlertType.NONE, startupError, ButtonType.OK);
            alert.setTitle("TellerFlow");
            alert.setHeaderText(null);
            alert.setGraphic(null);
            alert.showAndWait();
            Platform.exit();
            return;
        }
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        try {
            if (storage != null && model != null) {
                storage.saveUserPrefs(model.getUserPrefs());
            }
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        } finally {
            releaseAddressBookLock();
        }
    }

    private void releaseAddressBookLock() {
        if (addressBookLock != null) {
            try {
                addressBookLock.close();
            } catch (IOException e) {
                logger.warning("Could not release the client data lock.");
            }
            addressBookLock = null;
        }
    }
}
