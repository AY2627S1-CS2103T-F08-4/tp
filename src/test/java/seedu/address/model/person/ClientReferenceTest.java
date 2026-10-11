package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ClientReferenceTest {
    @Test
    public void constructor_normalizesCaseAndTrims() {
        ClientReference reference = new ClientReference("  sg-1024  ");
        assertEquals("SG-1024", reference.value);
        assertEquals(reference, new ClientReference("SG-1024"));
        assertEquals(reference.hashCode(), new ClientReference("sg-1024").hashCode());
        assertFalse(reference.equals(new ClientReference("SG-1025")));
    }

    @Test
    public void validation_enforcesLengthAndIdentifierCharacters() {
        assertTrue(ClientReference.isValidClientReference("C"));
        assertTrue(ClientReference.isValidClientReference("C" + "1".repeat(19)));
        for (String invalid : new String[] {"", " ", "1C", "-C", "C_1", "C 1", "C/1", "C" + "1".repeat(20)}) {
            assertFalse(ClientReference.isValidClientReference(invalid));
            assertThrows(IllegalArgumentException.class, () -> new ClientReference(invalid));
        }
        assertThrows(NullPointerException.class, () -> new ClientReference(null));
    }
}
