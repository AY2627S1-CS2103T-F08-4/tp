package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

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
    private Label clientReference;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        clientReference.setText(person.getClientReference() == null
                ? "" : person.getClientReference().value);
        clientReference.setVisible(person.getClientReference() != null);
        clientReference.setManaged(person.getClientReference() != null);
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

}
