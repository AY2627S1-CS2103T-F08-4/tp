package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.person.Address;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

class PersonDetailsPanelTest {
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
    void showPerson_contactCombinations_hidesMissingRowsAndRemovesTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            for (int mask = 0; mask < 8; mask++) {
                Phone phone = (mask & 1) == 0 ? null : new Phone("85355255");
                Email email = (mask & 2) == 0 ? null : new Email("john@example.com");
                Address address = (mask & 4) == 0 ? null : new Address("123 Main Street");
                Person person = new Person(new ClientReference("C0001"), new Name("John Doe"),
                        phone, email, address, null, null, null, Collections.emptySet());
                PersonDetailsPanel panel = new PersonDetailsPanel();
                panel.showPerson(person);
                Region root = (Region) ((ScrollPane) panel.getRoot()).getContent();

                assertLabel(root, "name", "John Doe");
                assertLabel(root, "reference", "C0001");
                assertLabel(root, "phone", phone == null ? "" : "85355255");
                assertLabel(root, "email", email == null ? "" : "john@example.com");
                assertLabel(root, "address", address == null ? "" : "123 Main Street");
                assertLabel(root, "cardDetails", "");
                assertLabel(root, "creditScore", "");
                assertLabel(root, "debt", "");
                assertEquals(mask != 0, root.lookup("#contactSection").isManaged());
                assertEquals(false, root.lookup("#financialSection").isManaged());
            }
        });
    }

    @Test
    void showPerson_completeProfile_preservesValuesAndCardMasking() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withClientReference("C0001")
                    .withCardDetails("1234567890123456", "123", "12/28", "Visa").build();
            PersonDetailsPanel panel = new PersonDetailsPanel();
            panel.showPerson(person);
            Region root = (Region) ((ScrollPane) panel.getRoot()).getContent();

            assertLabel(root, "name", PersonBuilder.DEFAULT_NAME);
            assertLabel(root, "phone", PersonBuilder.DEFAULT_PHONE);
            assertLabel(root, "email", PersonBuilder.DEFAULT_EMAIL);
            assertLabel(root, "address", PersonBuilder.DEFAULT_ADDRESS);
            assertLabel(root, "reference", "C0001");
            assertLabel(root, "cardDetails", "Visa ****3456 CVV: 123 expiry: 12/28");
            assertLabel(root, "creditScore", "700");
            assertLabel(root, "debt", "0.00");
        });
    }

    @Test
    void showPerson_financialDetailsWithoutContacts_displaysZeroValues() throws Exception {
        runOnFxThread(() -> {
            Person minimal = new Person(new ClientReference("C0001"), new Name("John Doe"),
                    null, null, null, null, null, null, Collections.emptySet());
            Person person = new PersonBuilder(minimal).withCreditScore("0").withDebt("0.00").build();
            PersonDetailsPanel panel = new PersonDetailsPanel();
            panel.showPerson(person);
            Region root = (Region) ((ScrollPane) panel.getRoot()).getContent();

            assertLabel(root, "phone", "");
            assertLabel(root, "email", "");
            assertLabel(root, "address", "");
            assertLabel(root, "cardDetails", "");
            assertLabel(root, "creditScore", "0");
            assertLabel(root, "debt", "0.00");
        });
    }

    @Test
    void formatCardDetails_completeCardDetails_masksCardNumberAndDisplaysCvvAndExpiry() {
        var person = new PersonBuilder()
                .withCardDetails("1234567890123456", "123", "12/28", "Visa")
                .build();

        assertEquals("Visa ****3456 CVV: 123 expiry: 12/28", PersonDetailsPanel.formatCardDetails(person));
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
    void formatTimestamp_rangeBoundaries_areConsistentAcrossTimeZones() {
        Instant minimum = Instant.parse("0001-01-01T18:00:00Z");
        Instant maximum = Instant.parse("9999-12-31T05:59:59.999999999Z");
        for (String zone : List.of("UTC", "Asia/Singapore", "-18:00", "+18:00")) {
            ZoneId displayZone = ZoneId.of(zone);
            assertTrue(PersonDetailsPanel.formatTimestamp(minimum, displayZone).startsWith("0001-"));
            assertTrue(PersonDetailsPanel.formatTimestamp(maximum, displayZone).startsWith("9999-"));
            for (Instant unsupported : List.of(minimum.minusNanos(1), maximum.plusNanos(1),
                    Instant.MIN, Instant.MAX)) {
                assertEquals("Timestamp information is invalid for this profile.",
                        PersonDetailsPanel.formatTimestamp(unsupported, displayZone));
            }
        }
        assertEquals("0001-01-01 00:00", PersonDetailsPanel.formatTimestamp(minimum, ZoneId.of("-18:00")));
        assertEquals("9999-12-31 23:59", PersonDetailsPanel.formatTimestamp(maximum, ZoneId.of("+18:00")));
    }

    @Test
    void formatTimestamps_unsupportedValues_showFallbackForEachAffectedField() {
        var person = new PersonBuilder().build();
        String unavailable = "Timestamp information is invalid for this profile.";
        assertEquals(new PersonDetailsPanel.TimestampDisplay(unavailable, "2026-10-12 14:18", ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(Instant.MAX, MODIFIED), DISPLAY_ZONE));
        assertEquals(new PersonDetailsPanel.TimestampDisplay("2026-03-15 09:42", unavailable, ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(CREATED, Instant.MIN), DISPLAY_ZONE));
        assertEquals(new PersonDetailsPanel.TimestampDisplay(unavailable, unavailable, ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(Instant.MIN, Instant.MAX), DISPLAY_ZONE));
        assertEquals(new PersonDetailsPanel.TimestampDisplay("", unavailable, ""),
                PersonDetailsPanel.formatTimestamps(person.withTimestamps(null, Instant.MAX), DISPLAY_ZONE));
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
            NodeList elements = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
                    .getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                ids.add(elements.item(i).getAttributes().getNamedItem("fx:id") == null ? ""
                        : elements.item(i).getAttributes().getNamedItem("fx:id").getNodeValue());
            }
        }
        assertTrue(ids.containsAll(Set.of("createdAt", "lastModifiedAt", "timestampUnavailable")));
        assertTrue(ids.indexOf("createdAt") < ids.indexOf("serviceNote"));
        assertTrue(ids.indexOf("lastModifiedAt") < ids.indexOf("serviceNote"));
    }

    @Test
    void formatCardDetails_referencedPersonWithoutCard_returnsEmptyText() {
        var withCard = new PersonBuilder().withClientReference("C0001").build();
        var withoutCard = new Person(withCard.getClientReference().orElse(null), withCard.getName(),
                withCard.getPhone().orElse(null), withCard.getEmail().orElse(null), withCard.getAddress().orElse(null),
                null, withCard.getCreditScore().orElse(null),
                withCard.getDebt().orElse(null), withCard.getTags());

        assertEquals("", PersonDetailsPanel.formatCardDetails(withoutCard));
    }

    private static void assertLabel(Region root, String id, String expectedText) {
        Node node = root.lookup("#" + id);
        String actualText = node instanceof TextInputControl
                ? ((TextInputControl) node).getText()
                : ((Label) node).getText();
        assertEquals(expectedText, actualText, id);
        Region row = (Region) root.lookup("#" + id + "Row");
        Region displayed = row == null ? (Region) node : row;
        assertEquals(!expectedText.isEmpty(), displayed.isVisible(), id + " visibility");
        assertEquals(!expectedText.isEmpty(), displayed.isManaged(), id + " layout space");
    }

    @Test
    void showPerson_longTag_wrapsInsideViewportWhenResized() throws Exception {
        runOnFxThread(() -> {
            String longTag = "RequiresSupervisorApprovalBeforeProcessingInternationalWireTransfersAboveDailyLimit";
            Person person = new PersonBuilder().withTags(longTag, "VIP").build();
            PersonDetailsPanel panel = new PersonDetailsPanel();
            panel.showPerson(person);
            StackPane container = new StackPane(panel.getRoot());
            Scene scene = new Scene(container);
            scene.getStylesheets().add(PersonDetailsPanel.class.getResource("/view/DarkTheme.css").toExternalForm());

            for (double width : new double[] {320, 480, 300}) {
                container.resize(width, 600);
                container.applyCss();
                container.layout();
                ScrollPane scrollPane = (ScrollPane) panel.getRoot();
                FlowPane tags = (FlowPane) scrollPane.lookup("#tags");
                Label label = tags.getChildren().stream()
                        .filter(Label.class::isInstance)
                        .map(Label.class::cast)
                        .filter(tagLabel -> longTag.equals(tagLabel.getText()))
                        .findFirst().orElseThrow(() -> new AssertionError("Long tag label is missing"));
                Bounds viewport = scrollPane.lookup(".viewport").localToScene(
                        scrollPane.lookup(".viewport").getLayoutBounds());
                Bounds renderedTag = label.localToScene(label.getLayoutBounds());

                assertEquals(longTag, label.getText());
                assertTrue(renderedTag.getMinX() >= viewport.getMinX() - 1);
                assertTrue(renderedTag.getMaxX() <= viewport.getMaxX() + 1,
                        "Full tag must fit within viewport at width " + width);
                Text text = (Text) label.lookup(".text");
                Text singleLine = new Text(longTag);
                singleLine.setFont(label.getFont());
                String diagnostics = "requestedWidth=" + width
                        + ", viewportWidth=" + viewport.getWidth()
                        + ", labelWidth=" + label.getWidth()
                        + ", labelHeight=" + label.getHeight()
                        + ", preferredHeight=" + label.prefHeight(label.getWidth())
                        + ", renderedText=[" + text.getText() + "]"
                        + ", wrappingWidth=" + text.getWrappingWidth()
                        + ", renderedTextHeight=" + text.getLayoutBounds().getHeight()
                        + ", singleLineHeight=" + singleLine.getLayoutBounds().getHeight()
                        + ", font=" + label.getFont()
                        + ", os=" + System.getProperty("os.name")
                        + ", architecture=" + System.getProperty("os.arch")
                        + ", javafxVersion=" + System.getProperty("javafx.version")
                        + ", javafxRuntimeVersion=" + System.getProperty("javafx.runtime.version");
                assertTrue(text.getLayoutBounds().getHeight() > singleLine.getLayoutBounds().getHeight(),
                        "Long tag should occupy multiple lines: " + diagnostics);
                assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1,
                        "All wrapped lines must have enough height: " + diagnostics);
            }
        });
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JavaFX card construction requires a display");
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
