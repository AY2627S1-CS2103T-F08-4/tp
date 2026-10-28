package seedu.address.commons.core;

/** Minimal entry point for the packaged logging regression test. */
public class LogsCenterProbe {
    public static void main(String[] args) {
        LogsCenter.getLogger(LogsCenterProbe.class).info("packaged logging probe");
    }
}
