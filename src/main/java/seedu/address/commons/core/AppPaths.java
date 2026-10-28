package seedu.address.commons.core;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/** Resolves files owned by the application relative to the packaged JAR. */
public final class AppPaths {
    private AppPaths() { }

    /** Uses the JAR's directory when packaged, or the working directory during development. */
    public static Path getAppHome() {
        try {
            if (AppPaths.class.getProtectionDomain().getCodeSource() == null) {
                return Paths.get("").toAbsolutePath();
            }
            Path source = Paths.get(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (source.getFileName() != null
                    && source.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                return source.toAbsolutePath().getParent();
            }
        } catch (URISyntaxException | SecurityException e) {
            // Development and restricted launch environments use the working directory.
        }
        return Paths.get("").toAbsolutePath();
    }

    public static Path resolve(String first, String... more) {
        return getAppHome().resolve(Paths.get(first, more));
    }
}
