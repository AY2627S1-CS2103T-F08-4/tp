package seedu.address.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/** Displays the currently selected client's details. */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";
    private static final String TIMESTAMP_UNAVAILABLE = "Timestamp information is unavailable for this profile.";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label emptyMessage;
    @FXML
    private VBox details;
    @FXML
    private VBox financialSection;
    @FXML
    private VBox contactSection;
    @FXML
    private HBox phoneRow;
    @FXML
    private HBox emailRow;
    @FXML
    private HBox addressRow;
    @FXML
    private Label reference;
    @FXML
    private Label name;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label serviceNote;
    @FXML
    private Label cardDetails;
    @FXML
    private Label creditScore;
    @FXML
    private Label debt;
    @FXML
    private FlowPane tags;
    @FXML
    private HBox cardDetailsRow;
    @FXML
    private HBox creditScoreRow;
    @FXML
    private HBox debtRow;
    @FXML
    private HBox tagsRow;
    @FXML
    private HBox createdAtRow;
    @FXML
    private HBox lastModifiedAtRow;
    @FXML
    private Label createdAt;
    @FXML
    private Label lastModifiedAt;
    @FXML
    private Label timestampUnavailable;

    /** Loads the details panel with no client selected. */
    public PersonDetailsPanel() {
        super(FXML);
        showPerson(null);
    }

    /** Shows the selected client, or the empty state when {@code person} is null. */
    public void showPerson(Person person) {
        boolean hasPerson = person != null;
        emptyMessage.setVisible(!hasPerson);
        emptyMessage.setManaged(!hasPerson);
        details.setVisible(hasPerson);
        details.setManaged(hasPerson);
        if (!hasPerson) {
            return;
        }

        reference.setText(person.getClientReference().map(value -> value.value).orElse("(none)"));
        name.setText(person.getName().toString());
        setOptionalRow(phoneRow, phone, person.getPhone().map(Object::toString).orElse(""));
        setOptionalRow(emailRow, email, person.getEmail().map(Object::toString).orElse(""));
        setOptionalRow(addressRow, address, person.getAddress().map(Object::toString).orElse(""));
        setShown(contactSection, phoneRow.isManaged() || emailRow.isManaged() || addressRow.isManaged());
        setOptionalRow(cardDetailsRow, cardDetails, formatCardDetails(person));
        setOptionalRow(creditScoreRow, creditScore, person.getCreditScore().map(Object::toString).orElse(""));
        setOptionalRow(debtRow, debt, person.getDebt().map(Object::toString).orElse(""));
        tags.getChildren().clear();
        person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label label = new Label(tag.tagName);
                    label.setWrapText(true);
                    label.setMinWidth(0);
                    label.maxWidthProperty().bind(tags.widthProperty());
                    tags.getChildren().add(label);
                });
        setShown(tagsRow, !tags.getChildren().isEmpty());
        setShown(financialSection, cardDetailsRow.isManaged() || creditScoreRow.isManaged()
                || debtRow.isManaged() || tagsRow.isManaged());

        TimestampDisplay timestamps = formatTimestamps(person, ZoneId.systemDefault());
        setOptionalRow(createdAtRow, createdAt, timestamps.created());
        setOptionalRow(lastModifiedAtRow, lastModifiedAt, timestamps.lastModified());
        setOptional(timestampUnavailable, timestamps.unavailable());
        serviceNote.setText(person.getServiceNote().map(value -> value.value).orElse("(none)"));
    }

    /** Returns a display-safe summary without the card verification value. */
    static String formatCardDetails(Person person) {
        return person.getCardDetails().map(details -> {
            String cardNumber = details.getCardNumber();
            String maskedCardNumber = cardNumber.length() <= 4
                    ? cardNumber
                    : "****" + cardNumber.substring(cardNumber.length() - 4);
            return details.getProvider() + " " + maskedCardNumber + " exp " + details.getExpiryDate();
        }).orElse("");
    }

    /** Returns the visible timestamp values for all combinations of known instants. */
    static TimestampDisplay formatTimestamps(Person person, ZoneId displayZone) {
        Instant created = person.getCreatedAt().orElse(null);
        Instant modified = person.getLastModifiedAt().orElse(null);
        if (created == null && modified == null) {
            return new TimestampDisplay("", "", TIMESTAMP_UNAVAILABLE);
        }
        String createdText = created == null ? "" : formatTimestamp(created, displayZone);
        String modifiedText = modified == null ? "" : formatTimestamp(modified, displayZone);
        return new TimestampDisplay(createdText, modifiedText, "");
    }

    /** Formats a known instant in the viewer's time zone. */
    static String formatTimestamp(Instant timestamp, ZoneId displayZone) {
        return TIMESTAMP_FORMAT.format(timestamp.atZone(displayZone));
    }

    record TimestampDisplay(String created, String lastModified, String unavailable) { }

    private static void setOptionalRow(Region row, Label value, String text) {
        value.setText(text);
        setShown(row, text != null && !text.isBlank());
    }

    private static void setOptional(Label label, String text) {
        label.setText(text);
        setShown(label, text != null && !text.isBlank());
    }

    private static void setShown(Region region, boolean shown) {
        region.setVisible(shown);
        region.setManaged(shown);
    }
}
