package seedu.address.model;

import java.util.Optional;

/** A working model whose changes become visible only after a successful save. */
public interface ModelStage {
    /** Returns the isolated model used to execute a command. */
    Model model();

    /** Publishes the staged changes to the source model. */
    void commit();

    /** Returns the timestamp action recorded by the command, if any. */
    Optional<String> timestampAction();
}
