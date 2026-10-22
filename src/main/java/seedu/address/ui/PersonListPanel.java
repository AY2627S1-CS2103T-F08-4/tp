package seedu.address.ui;

import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;
    private Consumer<Person> selectionHandler = unused -> { };

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((unused, oldPerson, newPerson) ->
                selectionHandler.accept(newPerson));
    }

    public void setSelectionHandler(Consumer<Person> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public Person getSelectedPerson() {
        return personListView.getSelectionModel().getSelectedItem();
    }

    /** Selects the row with this client reference, or the first row if none matches. */
    public void selectByReference(ClientReference reference) {
        for (int i = 0; i < personListView.getItems().size(); i++) {
            if (reference.equals(personListView.getItems().get(i).getClientReference())) {
                personListView.getSelectionModel().select(i);
                personListView.scrollTo(i);
                selectionHandler.accept(personListView.getItems().get(i));
                return;
            }
        }
        selectFirst();
    }

    /** Keeps a selected client after list refresh, falling back to the first visible row. */
    public void selectMatchingOrFirst(Person previous) {
        if (previous != null) {
            for (int i = 0; i < personListView.getItems().size(); i++) {
                if (previous.isSamePerson(personListView.getItems().get(i))) {
                    personListView.getSelectionModel().select(i);
                    selectionHandler.accept(personListView.getItems().get(i));
                    return;
                }
            }
        }
        selectFirst();
    }

    private void selectFirst() {
        if (personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().clearSelection();
            selectionHandler.accept(null);
        } else {
            personListView.getSelectionModel().selectFirst();
            selectionHandler.accept(personListView.getItems().getFirst());
        }
    }

    /** Returns the reference of the selected client, if one is selected. */
    ClientReference getSelectedClientReference() {
        Person selected = personListView.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getClientReference();
    }

    /** Restores selection after the list replaces a client with updated details. */
    void selectClientByReference(ClientReference reference) {
        if (reference == null) {
            return;
        }
        personListView.getItems().stream()
                .filter(person -> reference.equals(person.getClientReference()))
                .findFirst()
                .ifPresent(person -> personListView.getSelectionModel().select(person));
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
                setText(null);
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
