package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.AddressBookFileLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;

public class MainAppInstanceLockTest {
    @TempDir
    public Path testFolder;

    @Test
    public void isStorageInitialized_secondInstance_cannotOverwriteFirstInstance() throws Exception {
        Path dataPath = testFolder.resolve("data/addressbook.json");
        Path prefsPath = testFolder.resolve("prefs.json");
        new JsonAddressBookStorage(dataPath).saveAddressBook(new AddressBook());
        MainApp first = new MainApp();
        MainApp second = new MainApp();
        try {
            assertTrue(first.isStorageInitialized(prefsPath, dataPath));
            first.logic.execute("add r/ALPHABASE n/Base Client");
            String prefsBefore = Files.readString(prefsPath);
            assertFalse(second.isStorageInitialized(prefsPath, dataPath));
            second.stop();
            assertEquals(prefsBefore, Files.readString(prefsPath));
            first.logic.execute("add r/ALPHAONE n/Alice Client");
        } finally {
            first.stop();
            second.stop();
        }

        MainApp restarted = new MainApp();
        try {
            assertTrue(restarted.isStorageInitialized(prefsPath, dataPath));
            assertEquals(2, restarted.model.getAddressBook().getPersonList().size());
            restarted.logic.execute("add r/ALPHATWO n/Bob Client");
            assertEquals(3, new JsonAddressBookStorage(dataPath).readAddressBook()
                    .orElseThrow().getPersonList().size());
        } finally {
            restarted.stop();
        }
    }

    @Test
    public void isStorageInitialized_unwritableLockPath_blocksStartupWithoutChangingData() throws Exception {
        Path dataPath = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(dataPath).saveAddressBook(new AddressBook());
        String original = Files.readString(dataPath);
        Files.createDirectory(dataPath.resolveSibling("addressbook.json.lock"));
        Path prefsPath = testFolder.resolve("prefs.json");
        MainApp app = new MainApp();

        assertFalse(app.isStorageInitialized(prefsPath, dataPath));
        app.stop();

        assertEquals(original, Files.readString(dataPath));
        assertFalse(Files.exists(prefsPath));
    }

    @Test
    public void isStorageInitialized_initializationFails_releasesLock() throws Exception {
        Path dataPath = testFolder.resolve("addressbook.json");
        MainApp broken = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                throw new IllegalStateException("Simulated startup failure");
            }
        };
        Path prefsPath = testFolder.resolve("prefs.json");
        assertThrows(IllegalStateException.class, () -> broken.isStorageInitialized(prefsPath, dataPath));
        try (AddressBookFileLock lock = AddressBookFileLock.tryAcquire(dataPath).orElseThrow()) {
            assertTrue(Files.notExists(dataPath));
        }
    }

    @Test
    public void start_secondInstance_showsDialogAndExitsNormally() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "The startup dialog requires a display");
        Path dataPath = testFolder.resolve("addressbook.json");
        Path outputPath = testFolder.resolve("second-instance.log");
        List<String> classPath = new ArrayList<>();
        classPath.add(System.getProperty("java.class.path"));
        for (ClassLoader loader = getClass().getClassLoader(); loader != null; loader = loader.getParent()) {
            if (loader instanceof URLClassLoader urlLoader) {
                for (URL url : urlLoader.getURLs()) {
                    classPath.add(Path.of(url.toURI()).toString());
                }
            }
        }
        try (AddressBookFileLock lock = AddressBookFileLock.tryAcquire(dataPath).orElseThrow()) {
            Process child = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-cp", String.join(File.pathSeparator, classPath), StartupDialogProbe.class.getName(),
                    dataPath.toString()).redirectErrorStream(true).redirectOutput(outputPath.toFile()).start();
            try {
                assertTrue(child.waitFor(20, TimeUnit.SECONDS), "Second instance did not close its error dialog");
                String output = Files.readString(outputPath);
                assertEquals(0, child.exitValue(), output);
                assertTrue(output.contains("STARTUP_DIALOG_SHOWN"), output);
                assertFalse(output.contains("Exception in Application start method"), output);
                assertTrue(AddressBookFileLock.tryAcquire(dataPath).isEmpty(), "First instance must keep its lock");
            } finally {
                child.destroyForcibly();
                child.waitFor(10, TimeUnit.SECONDS);
            }
        }
    }

}
