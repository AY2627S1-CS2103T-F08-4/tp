package seedu.address.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String TIMESTAMP_UNAVAILABLE = "Timestamp information is unavailable for this profile.";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label clientReference;
    @FXML
    private Label cardDetails;
    @FXML
    private Label creditScore;
    @FXML
    private Label debt;
    @FXML
    private Label createdAt;
    @FXML
    private Label lastModifiedAt;
    @FXML
    private Label timestampUnavailable;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        name.setTooltip(new Tooltip(person.getName().fullName));
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        if (person.getClientReference() == null) {
            setOptionalLabel(clientReference, "");
        } else {
            String reference = person.getClientReference().value;
            setOptionalLabel(clientReference, reference);
            clientReference.setTooltip(new Tooltip("Client reference: " + reference));
        }
        setOptionalLabel(cardDetails, formatCardDetails(person));
        setOptionalLabel(creditScore, person.getCreditScore() == null
                ? "" : "Credit score: " + person.getCreditScore());
        setOptionalLabel(debt, person.getDebt() == null
                ? "" : "Debt: " + person.getDebt());
        TimestampDisplay timestamps = formatTimestamps(person, ZoneId.systemDefault());
        setOptionalLabel(createdAt, timestamps.created());
        setOptionalLabel(lastModifiedAt, timestamps.lastModified());
        setOptionalLabel(timestampUnavailable, timestamps.unavailable());
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    /** Returns a display-safe summary of a person's card details. */
    static String formatCardDetails(Person person) {
        if (person.getCardDetails() == null) {
            return "";
        }
        String cardNumber = person.getCardDetails().getCardNumber();
        String maskedCardNumber = cardNumber.length() <= 4
                ? cardNumber
                : "****" + cardNumber.substring(cardNumber.length() - 4);
        return "Card: " + person.getCardDetails().getProvider()
                + " " + maskedCardNumber
                + " exp " + person.getCardDetails().getExpiryDate();
    }

    /** Returns the visible timestamp text for all combinations of known values. */
    static TimestampDisplay formatTimestamps(Person person, ZoneId displayZone) {
        Instant created = person.getCreatedAt();
        Instant modified = person.getLastModifiedAt();
        if (created == null && modified == null) {
            return new TimestampDisplay("", "", TIMESTAMP_UNAVAILABLE);
        }
        String createdText = created == null ? "" : "Created: " + formatTimestamp(created, displayZone);
        String modifiedText = modified == null ? "" : "Last Modified: " + formatTimestamp(modified, displayZone);
        return new TimestampDisplay(createdText, modifiedText, "");
    }

    /** Formats a known instant in the viewer's time zone. */
    static String formatTimestamp(Instant timestamp, ZoneId displayZone) {
        return TIMESTAMP_FORMAT.format(timestamp.atZone(displayZone));
    }

    record TimestampDisplay(String created, String lastModified, String unavailable) { }

    private static void setOptionalLabel(Label label, String text) {
        boolean hasValue = ((text != null) && (!text.isBlank()));
        label.setText(text);
        label.setVisible(hasValue);
        label.setManaged(hasValue);
    }
}
