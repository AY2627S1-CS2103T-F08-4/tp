package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/** Holds exclusive access to an address book for the lifetime of an application instance. */
public final class AddressBookFileLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private AddressBookFileLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Tries to acquire the address book's companion lock file. An existing lock-file name is harmless:
     * only an active operating-system lock prevents access, and process termination releases that lock.
     *
     * @return empty if another instance already holds the lock
     * @throws IOException if exclusive access cannot be established
     */
    public static Optional<AddressBookFileLock> tryAcquire(Path dataPath) throws IOException {
        requireNonNull(dataPath);
        Path absolutePath = dataPath.toAbsolutePath().normalize();
        Files.createDirectories(absolutePath.getParent());
        Path lockPath = absolutePath.resolveSibling(absolutePath.getFileName() + ".lock");
        FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        boolean acquired = false;
        try {
            FileLock lock;
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                return Optional.empty();
            }
            if (lock == null) {
                return Optional.empty();
            }
            acquired = true;
            return Optional.of(new AddressBookFileLock(channel, lock));
        } finally {
            if (!acquired) {
                channel.close();
            }
        }
    }

    /** Releases access, retaining the companion file to avoid races between waiting instances. */
    @Override
    public void close() throws IOException {
        try {
            if (lock.isValid()) {
                lock.release();
            }
        } finally {
            channel.close();
        }
    }
}
