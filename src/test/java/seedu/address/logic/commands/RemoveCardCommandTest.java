package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.ClientReference;

public class RemoveCardCommandTest {

    @Test
    public void equals_sameReferenceIsEqual() {
        RemoveCardCommand command = new RemoveCardCommand(new ClientReference("C0001"));
        RemoveCardCommand sameReference = new RemoveCardCommand(new ClientReference("c0001"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameReference));
        assertTrue(sameReference.equals(command));
        assertFalse(command.equals(new RemoveCardCommand(new ClientReference("C0002"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
    }

    @Test
    public void hashCode_equalCommandsHaveSameHashCode() {
        RemoveCardCommand command = new RemoveCardCommand(new ClientReference("C0001"));
        RemoveCardCommand sameReference = new RemoveCardCommand(new ClientReference("c0001"));

        assertEquals(command.hashCode(), sameReference.hashCode());
        Set<RemoveCardCommand> commands = new HashSet<>();
        commands.add(command);
        commands.add(sameReference);
        assertEquals(1, commands.size());
    }

    @Test
    public void toStringMethod_containsNormalizedReference() {
        RemoveCardCommand command = new RemoveCardCommand(new ClientReference("c0001"));

        assertEquals(RemoveCardCommand.class.getCanonicalName() + "{reference=C0001}", command.toString());
    }
}
