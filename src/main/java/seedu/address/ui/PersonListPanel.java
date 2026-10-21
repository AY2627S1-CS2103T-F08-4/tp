package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /** Returns the client selected before a command changes the displayed list. */
    public Person getSelectedPerson() {
        return personListView.getSelectionModel().getSelectedItem();
    }

    /** Applies a command's selection request after its list changes have completed. */
    public void applySelection(CommandResult result, Person previouslySelected) {
        if (result.getSelectionRequest() == CommandResult.SelectionRequest.NONE) {
            return;
        }
        Person desired = result.getSelectionRequest() == CommandResult.SelectionRequest.SELECT_PERSON
                ? result.getPersonToSelect() : previouslySelected;
        ObservableList<Person> displayed = personListView.getItems();
        for (int i = 0; i < displayed.size(); i++) {
            Person candidate = displayed.get(i);
            if (desired != null && (candidate.equals(desired)
                    || desired.getClientReference() != null
                    && desired.getClientReference().equals(candidate.getClientReference()))) {
                personListView.getSelectionModel().select(i);
                return;
            }
        }
        if (displayed.isEmpty()) {
            personListView.getSelectionModel().clearSelection();
        } else {
            personListView.getSelectionModel().selectFirst();
        }
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
