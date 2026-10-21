package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/** Displays the currently selected client's details. */
public class PersonDetailsPanel extends UiPart<Region> {
    private static final String FXML = "PersonDetailsPanel.fxml";

    @FXML
    private Label emptyMessage;
    @FXML
    private VBox details;
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

        reference.setText("Reference: " + (person.getClientReference() == null
                ? "(none)" : person.getClientReference().value));
        name.setText("Name: " + person.getName());
        phone.setText("Phone: " + person.getPhone());
        email.setText("Email: " + person.getEmail());
        address.setText("Address: " + person.getAddress());
        serviceNote.setText("Service note: " + (person.getServiceNote() == null
                ? "(none)" : person.getServiceNote().value));
        setOptional(cardDetails, PersonCard.formatCardDetails(person));
        setOptional(creditScore, person.getCreditScore() == null
                ? "" : "Credit score: " + person.getCreditScore());
        setOptional(debt, person.getDebt() == null ? "" : "Debt: " + person.getDebt());
        tags.getChildren().clear();
        person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        tags.setVisible(!tags.getChildren().isEmpty());
        tags.setManaged(!tags.getChildren().isEmpty());
    }

    private static void setOptional(Label label, String text) {
        label.setText(text);
        label.setVisible(!text.isBlank());
        label.setManaged(!text.isBlank());
    }
}
