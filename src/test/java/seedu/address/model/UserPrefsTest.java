package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.JsonUtil;

public class UserPrefsTest {

    @Test
    public void isValid_defaultPreferences_returnsTrue() {
        assertTrue(new UserPrefs().isValid());
    }

    @Test
    public void isValid_nullGuiSettingsFromJson_returnsFalse() throws IOException {
        UserPrefs userPrefs = JsonUtil.fromJsonString("{\"guiSettings\":null}", UserPrefs.class);
        assertFalse(userPrefs.isValid());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

}
