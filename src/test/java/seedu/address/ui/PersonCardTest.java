package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.Address;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

class PersonCardTest {
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Singapore");
    private static final Instant CREATED = Instant.parse("2026-03-15T01:42:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-10-12T06:18:00Z");


    @BeforeAll
    static void initializeToolkit() {
        if (!GraphicsEnvironment.isHeadless()) {
            Platform.startup(() -> Platform.setImplicitExit(false));
        }
    }

    @Test
    void constructor_contactCombinations_hidesMissingLabelsAndRemovesTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            for (int mask = 0; mask < 8; mask++) {
                Phone phone = (mask & 1) == 0 ? null : new Phone("85355255");
                Email email = (mask & 2) == 0 ? null : new Email("john@example.com");
                Address address = (mask & 4) == 0 ? null : new Address("123 Main Street");
                Person person = new Person(new ClientReference("C0001"), new Name("John Doe"),
                        phone, email, address, null, null, null, Collections.emptySet());
                Region root = new PersonCard(person, 1).getRoot();

                assertLabel(root, "id", "1. ");
                assertLabel(root, "name", "John Doe");
                assertLabel(root, "clientReference", "Client reference: C0001");
                assertLabel(root, "phone", phone == null ? "" : "85355255");
                assertLabel(root, "email", email == null ? "" : "john@example.com");
                assertLabel(root, "address", address == null ? "" : "123 Main Street");
                assertLabel(root, "cardDetails", "");
                assertLabel(root, "creditScore", "");
                assertLabel(root, "debt", "");
            }
        });
    }

    @Test
    void constructor_completeProfile_preservesValuesAndCardMasking() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withClientReference("C0001")
                    .withCardDetails("1234567890123456", "123", "12/28", "Visa").build();
            Region root = new PersonCard(person, 2).getRoot();

            assertLabel(root, "id", "2. ");
            assertLabel(root, "name", PersonBuilder.DEFAULT_NAME);
            assertLabel(root, "phone", PersonBuilder.DEFAULT_PHONE);
            assertLabel(root, "email", PersonBuilder.DEFAULT_EMAIL);
            assertLabel(root, "address", PersonBuilder.DEFAULT_ADDRESS);
            assertLabel(root, "clientReference", "Client reference: C0001");
            assertLabel(root, "cardDetails", "Card: Visa ****3456 exp 12/28");
            assertLabel(root, "creditScore", "Credit score: 700");
            assertLabel(root, "debt", "Debt: 0.00");
        });
    }

    @Test
    void constructor_financialDetailsWithoutContacts_displaysZeroValues() throws Exception {
        runOnFxThread(() -> {
            Person minimal = new Person(new ClientReference("C0001"), new Name("John Doe"),
                    null, null, null, null, null, null, Collections.emptySet());
            Person person = new PersonBuilder(minimal).withCreditScore("0").withDebt("0.00").build();
            Region root = new PersonCard(person, 1).getRoot();

            assertLabel(root, "phone", "");
            assertLabel(root, "email", "");
            assertLabel(root, "address", "");
            assertLabel(root, "cardDetails", "");
            assertLabel(root, "creditScore", "Credit score: 0");
            assertLabel(root, "debt", "Debt: 0.00");
        });
    }

    @Test
    void formatCardDetails_completeCardDetails_masksCardNumberAndOmitsCvv() {
        var person = new PersonBuilder()
                .withCardDetails("1234567890123456", "123", "12/28", "Visa")
                .build();

        assertEquals("Card: Visa ****3456 exp 12/28", PersonCard.formatCardDetails(person));
    }

    @Test
    void formatCardDetails_legacyPerson_returnsEmptyText() {
        var person = new Person(
                new Name("Legacy Person"),
                new Phone("85355255"),
                new Email("legacy@example.com"),
                new Address("123 Legacy Street"),
                Collections.emptySet());

        assertEquals("", PersonCard.formatCardDetails(person));
    }

    @Test
    void formatTimestamp_usesViewerTimeZone() {
        assertEquals("2026-03-15 09:42", PersonCard.formatTimestamp(
                CREATED, DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_bothKnown_showsBoth() {
        var person = new PersonBuilder().build().withTimestamps(CREATED, MODIFIED);

        assertEquals(new PersonCard.TimestampDisplay("Created: 2026-03-15 09:42",
                "Last Modified: 2026-10-12 14:18", ""), PersonCard.formatTimestamps(person, DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_oneKnown_showsOnlyKnownField() {
        var person = new PersonBuilder().build();

        assertEquals(new PersonCard.TimestampDisplay("", "Last Modified: 2026-10-12 14:18", ""),
                PersonCard.formatTimestamps(person.withTimestamps(null, MODIFIED), DISPLAY_ZONE));
        assertEquals(new PersonCard.TimestampDisplay("Created: 2026-03-15 09:42", "", ""),
                PersonCard.formatTimestamps(person.withTimestamps(CREATED, null), DISPLAY_ZONE));
    }

    @Test
    void formatTimestamps_noneKnown_showsOneMessage() {
        var person = new PersonBuilder().build();

        assertEquals(new PersonCard.TimestampDisplay("", "",
                "Timestamp information is unavailable for this profile."),
                PersonCard.formatTimestamps(person, DISPLAY_ZONE));
    }

    @Test
    void timestampLabels_existInCardFxml() throws Exception {
        Set<String> ids = new HashSet<>();
        try (InputStream stream = PersonCard.class.getResourceAsStream("/view/PersonListCard.fxml")) {
            NodeList labels = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
                    .getElementsByTagName("Label");
            for (int i = 0; i < labels.getLength(); i++) {
                ids.add(labels.item(i).getAttributes().getNamedItem("fx:id") == null ? ""
                        : labels.item(i).getAttributes().getNamedItem("fx:id").getNodeValue());
            }
        }
        assertTrue(ids.containsAll(Set.of("createdAt", "lastModifiedAt", "timestampUnavailable")));
    }

    private static void assertLabel(Region root, String id, String expectedText) {
        Label label = (Label) root.lookup("#" + id);
        assertEquals(expectedText, label.getText(), id);
        assertEquals(!expectedText.isEmpty(), label.isVisible(), id + " visibility");
        assertEquals(!expectedText.isEmpty(), label.isManaged(), id + " layout space");
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JavaFX card construction requires a display");
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
