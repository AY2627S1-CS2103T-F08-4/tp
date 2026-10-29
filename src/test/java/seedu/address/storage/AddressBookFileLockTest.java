package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AddressBookFileLockTest {
    @TempDir
    public Path testFolder;

    @Test
    void acquire_sameDataPath_excludesAnotherInstanceUntilClosed() throws Exception {
        Path path = testFolder.resolve("data/addressbook.json");
        try (AddressBookFileLock first = AddressBookFileLock.tryAcquire(path).orElseThrow()) {
            assertTrue(AddressBookFileLock.tryAcquire(path).isEmpty());
            assertFalse(Files.exists(path), "Acquiring a lock must not create client data");
            try (AddressBookFileLock separate = AddressBookFileLock.tryAcquire(
                    testFolder.resolve("other/addressbook.json")).orElseThrow()) {
                assertTrue(AddressBookFileLock.tryAcquire(path).isEmpty());
            }
        }
        try (AddressBookFileLock reopened = AddressBookFileLock.tryAcquire(path).orElseThrow()) {
            assertTrue(AddressBookFileLock.tryAcquire(path).isEmpty());
        }
    }

    @Test
    void acquire_otherProcess_excludesWritersAndReleasesAfterCrash() throws Exception {
        Path path = testFolder.resolve("addressbook.json");
        String classPath = Path.of(FileLockProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator + Path.of(AddressBookFileLock.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
        Process child = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classPath, FileLockProbe.class.getName(), path.toString()).redirectErrorStream(true).start();
        try (BufferedReader output = new BufferedReader(new InputStreamReader(
                child.getInputStream(), StandardCharsets.UTF_8))) {
            FutureTask<String> ready = new FutureTask<>(output::readLine);
            Thread.startVirtualThread(ready);
            assertEquals("LOCKED", ready.get(10, TimeUnit.SECONDS));
            assertTrue(AddressBookFileLock.tryAcquire(path).isEmpty());
        } finally {
            child.destroyForcibly();
            assertTrue(child.waitFor(10, TimeUnit.SECONDS));
        }

        assertTrue(Files.exists(path.resolveSibling("addressbook.json.lock")));
        try (AddressBookFileLock reopened = AddressBookFileLock.tryAcquire(path).orElseThrow()) {
            assertTrue(AddressBookFileLock.tryAcquire(path).isEmpty());
        }
    }
}
