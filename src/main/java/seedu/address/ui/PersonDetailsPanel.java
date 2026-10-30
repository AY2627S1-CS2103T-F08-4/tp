package seedu.address.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.util.TimestampUtil;
import seedu.address.model.person.Person;

/** Displays the currently selected client's details. */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";
    private static final double SCROLL_SPEED_MULTIPLIER = 2.5;
    private static final String TIMESTAMP_UNAVAILABLE = "Timestamp information is unavailable for this profile.";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label emptyMessage;
    @FXML
    private ScrollPane detailsScrollPane;
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
    private TextField reference;
    @FXML
    private Label name;
    @FXML
    private TextField phone;
    @FXML
    private TextField email;
    @FXML
    private TextField address;
    @FXML
    private TextArea serviceNote;
    @FXML
    private TextField cardDetails;
    @FXML
    private TextField creditScore;
    @FXML
    private TextField debt;
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
    private TextField createdAt;
    @FXML
    private TextField lastModifiedAt;
    @FXML
    private Label timestampUnavailable;

    /** Loads the details panel with no client selected. */
    public PersonDetailsPanel() {
        super(FXML);
        detailsScrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
        showPerson(null);
    }

    /** Scrolls the details pane faster so it feels consistent with the client list. */
    private void handleScroll(ScrollEvent event) {
        detailsScrollPane.setVvalue(detailsScrollPane.getVvalue()
                - event.getDeltaY() * SCROLL_SPEED_MULTIPLIER / 100.0);
        event.consume();
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

    /** Returns a summary of the card details with the card number masked. */
    static String formatCardDetails(Person person) {
        return person.getCardDetails().map(details -> {
            String cardNumber = details.getCardNumber();
            String maskedCardNumber = cardNumber.length() <= 4
                    ? cardNumber
                    : "****" + cardNumber.substring(cardNumber.length() - 4);
            return details.getProvider() + " " + maskedCardNumber
                    + " CVV: " + details.getCvv()
                    + " expiry: " + details.getExpiryDate();
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

    /** Formats a known instant, or reports unavailable information outside the supported display range. */
    static String formatTimestamp(Instant timestamp, ZoneId displayZone) {
        if (!TimestampUtil.isDisplayable(timestamp)) {
            return TimestampUtil.INVALID_TIMESTAMP_MESSAGE;
        }
        return TIMESTAMP_FORMAT.format(timestamp.atZone(displayZone));
    }

    record TimestampDisplay(String created, String lastModified, String unavailable) { }

    private static void setOptionalRow(Region row, TextField value, String text) {
        value.setText(text);
        setShown(row, text != null && !text.isBlank());
    }

    private static void setOptional(Label label, String text) {
        label.setText(text);
        setShown(label, text != null && !text.isBlank());
    }

    private static void setShown(Region region, boolean isShown) {
        region.setVisible(isShown);
        region.setManaged(isShown);
    }
}
