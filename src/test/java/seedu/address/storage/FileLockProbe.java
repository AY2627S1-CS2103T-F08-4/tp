package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/** A separate process used to verify lock exclusion and release after a crash. */
public class FileLockProbe {
    public static void main(String[] args) throws IOException {
        Optional<AddressBookFileLock> acquired = AddressBookFileLock.tryAcquire(Path.of(args[0]));
        if (acquired.isEmpty()) {
            System.out.println("BLOCKED");
            return;
        }
        try (AddressBookFileLock lock = acquired.get()) {
            System.out.println("LOCKED");
            System.out.flush();
            System.in.read();
        }
    }
}
