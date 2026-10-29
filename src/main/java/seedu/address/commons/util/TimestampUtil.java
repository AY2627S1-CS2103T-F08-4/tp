package seedu.address.commons.util;

import java.time.Instant;

/** Defines which timestamps can be displayed within years 0001–9999 in every supported offset. */
public final class TimestampUtil {
    public static final String INVALID_TIMESTAMP_MESSAGE = "Timestamp information is invalid for this profile.";

    // Account for the full supported offset range, from -18:00 to +18:00.
    private static final Instant MIN_DISPLAYABLE = Instant.parse("0001-01-01T18:00:00Z");
    private static final Instant MAX_DISPLAYABLE = Instant.parse("9999-12-31T05:59:59.999999999Z");

    private TimestampUtil() { }

    /** Returns whether a known timestamp is safe to display regardless of the viewer's time zone. */
    public static boolean isDisplayable(Instant timestamp) {
        return !timestamp.isBefore(MIN_DISPLAYABLE) && !timestamp.isAfter(MAX_DISPLAYABLE);
    }
}
