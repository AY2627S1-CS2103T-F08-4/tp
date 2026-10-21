package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

class PersonDetailsPanelTest {
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Singapore");
    private static final Instant CREATED = Instant.parse("2026-03-15T01:42:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-10-12T06:18:00Z");


    @Test
    void formatCardDetails_completeCardDetails_masksCardNumberAndOmitsCvv() {
        var person = new PersonBuilder()
                .withCardDetails("1234567890123456", "123", "12/28", "Visa")
                .build();

        assertEquals("Visa ****3456 exp 12/28", PersonDetailsPanel.formatCardDetails(person));
    }

    @Test
    void formatCardDetails_legacyPerson_returnsEmptyText() {
        var person = new Person(
                new Name("Legacy Person"),
                new Phone("85355255"),
                new Email("legacy@example.com"),
                new Address("123 Legacy Street"),
                Collections.emptySet());

        assertEquals("", PersonDetailsPanel.formatCardDetails(person));
    }

    @Test
    void formatTimestamp_usesViewerTimeZone() {
        assertEquals("2026-03-15 09:42", PersonDetailsPanel.formatTimestamp(
                CREATED, DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_bothKnown_showsBoth() {
        var person = new PersonBuilder().build().withTimestamps(CREATED, MODIFIED);

        assertEquals(new PersonDetailsPanel.TimestampDisplay("2026-03-15 09:42",
                "2026-10-12 14:18", ""), PersonDetailsPanel.formatTimestamps(person, DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_oneKnown_showsOnlyKnownField() {
        var person = new PersonBuilder().build();

        assertEquals(new PersonDetailsPanel.TimestampDisplay("", "2026-10-12 14:18", ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(null, MODIFIED), DISPLAY_ZONE));
        assertEquals(new PersonDetailsPanel.TimestampDisplay("2026-03-15 09:42", "", ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(CREATED, null), DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_noneKnown_showsOneMessage() {
        var person = new PersonBuilder().build();

        assertEquals(new PersonDetailsPanel.TimestampDisplay("", "",
                "Timestamp information is unavailable for this profile."),
                PersonDetailsPanel.formatTimestamps(person, DISPLAY_ZONE));
    }

    @Test
    void timestampLabels_existBeforeServiceNoteInDetailsFxml() throws Exception {
        List<String> ids = new ArrayList<>();
        try (InputStream stream = PersonDetailsPanel.class.getResourceAsStream("/view/PersonDetailsPanel.fxml")) {
            NodeList labels = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
                    .getElementsByTagName("Label");
            for (int i = 0; i < labels.getLength(); i++) {
                ids.add(labels.item(i).getAttributes().getNamedItem("fx:id") == null ? ""
                        : labels.item(i).getAttributes().getNamedItem("fx:id").getNodeValue());
            }
        }
        assertTrue(ids.containsAll(Set.of("createdAt", "lastModifiedAt", "timestampUnavailable")));
        assertEquals("serviceNote", ids.getLast());
    }
}
