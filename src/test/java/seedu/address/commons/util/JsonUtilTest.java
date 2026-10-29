package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    @Test
    public void fromJsonString_trailingContent_throwsIoException() {
        for (String tail : new String[] {" THIS IS NOT JSON", " {}", " []", " null", " 1", " true"}) {
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{}" + tail,
                    SerializableTestClass.class), tail);
        }
    }

    @Test
    public void fromJsonString_trailingWhitespace_isAccepted() throws IOException {
        SerializableTestClass actual = JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION + " \r\n\t", SerializableTestClass.class);
        assertEquals(SerializableTestClass.getNameTestValue(), actual.getName());
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
