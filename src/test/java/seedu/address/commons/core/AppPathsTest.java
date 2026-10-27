package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class AppPathsTest {
    @Test
    public void developmentLaunch_usesWorkingDirectory() {
        assertEquals(Paths.get("").toAbsolutePath(), AppPaths.getAppHome());
        assertEquals(Paths.get("").toAbsolutePath().resolve("data").resolve("addressbook.json"),
                AppPaths.resolve("data", "addressbook.json"));
    }
}
