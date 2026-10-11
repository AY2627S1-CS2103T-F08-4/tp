package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CardDetailsTest {

    @Test
    public void constructor_validExpiryDate_success() {
        for (String expiryDate : new String[] {"01/00", "02/24", "09/28", "10/28", "12/99"}) {
            CardDetails details = new CardDetails("4111111111111111", "123", expiryDate, "Visa");
            assertEquals(expiryDate, details.getExpiryDate());
        }
    }

    @Test
    public void constructor_invalidExpiryDate_throwsIllegalArgumentException() {
        for (String expiryDate : new String[] {"", " ", "00/28", "13/28", "1/28", "01/8", "01/2028",
            "01-28", "01/28/01", "aa/bb", "01 /28", " 01/28 ", "01/28\n"}) {
            assertThrows(IllegalArgumentException.class, CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS, ()
                    -> new CardDetails("4111111111111111", "123", expiryDate, "Visa"));
        }
    }
}
