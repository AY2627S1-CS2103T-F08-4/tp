---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

---

## **Acknowledgements**

- _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

---

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

---

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The **_Architecture Diagram_** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.

- At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
- At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

- [**`UI`**](#ui-component): The UI of the App.
- [**`Logic`**](#logic-component): The command executor.
- [**`Model`**](#model-component): Holds the data of the App in memory.
- [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The _Sequence Diagram_ below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

- defines its _API_ in an `interface` with the same name as the Component.
- provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

- executes user commands using the `Logic` component.
- listens for changes to `Model` data so that the UI can be updated with the modified data.
- keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
- depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:

- When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
- All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component

**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />

The `Model` component,

- stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
- stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
- stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
- does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

- can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
- is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
- depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

---

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

- `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
- `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
- `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

- **Alternative 1 (current choice):** Saves the entire address book.
  - Pros: Easy to implement.
  - Cons: May have performance issues in terms of memory usage.

- **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  - Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  - Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_

---

## **Documentation, logging, testing, dev-ops**

- [Documentation guide](Documentation.md)
- [Testing guide](Testing.md)
- [Logging guide](Logging.md)
- [DevOps guide](DevOps.md)

---

## **Appendix: Requirements**

### Product scope

**Target user profile**:

- is a Bank Teller
- works for a small bank
- has no existing easy client information access
- prefers desktop apps over other types of applications
- can type fast
- is reasonably comfortable using CLI apps

**Value proposition**:
Provides a fast and convenient CLI-based method of managing bank clients for bank tellers. Information like their card numbers and credit scores can be managed. Credit scores can conveniently be tracked and computed per user, allowing bankers to better manage them.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …      | I want to …                                                                                     | So that I can…                                                             |
| -------- | ----------- |-------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `* * *`  | bank teller | create a client profile containing a unique client reference, name, contact and contact details | keep the client's information in one place.                                |
| `* * *`  | bank teller | view my saved client profiles and their card and contact details                                | retrieve the information needed when serving a client.                     |
| `* * *`  | bank teller | delete a client profile                                                                         | remove records that were created by mistake or are no longer needed.       |
| `* * *`  | bank teller | access my saved client records after closing and reopening TellerFlow                           | do not have to enter the same information again.                           |
| `* *`    | bank teller | update a client's contact details without recreating the profile                                | keep the record accurate with less repeated work.                          |
| `* *`    | bank teller | create a profile before all optional contact details are available                              | record a client without inventing missing information.                     |
| `* *`    | bank teller | receive a warning when a new profile shares an existing phone number or email address           | check for accidental duplicates while allowing legitimate shared contacts. |
| `* *`    | bank teller | find clients using all or part of their names                                                   | locate a record when I do not remember the exact name.                     |
| `* *`    | bank teller | find clients using a phone number                                                               | locate a record when the number is the information available to me.        |
| `* *`    | bank teller | find clients using a card number                                                                | locate a record when the card number is the information available to me.   |
| `* *`    | bank teller | maintain a short service note on a client profile                                               | retain relevant context for the next interaction.                          |
| `* *`    | bank teller | remove an obsolete card summary from a client profile                                           | do not mistake an old card for a current one.                              |
| `* *`    | bank teller | see when a client profile was created and last modified                                         | judge how recently its information was maintained.                         |
| `* *`    | bank teller | store my clients' personal and card details securely                                            | keep their private details confidential.                                   |
| `*`      | bank teller | access help on available commands and their usage                                               | complete tasks when I do not remember the required input.                  |
| `*`      | bank teller | find clients with recorded cards expiring in a month                                            | identify records that may need a follow-up.                                |
| `*`      | bank teller | sort the displayed client list by name                                                          | scan the records in a predictable order.                                   |

### Use cases

For all use cases below, the **Actor** is the bank teller and the **System** is TellerFlow. These use cases describe the intended product, including features that are not yet available.

#### Use case: Create a client profile

##### Main success scenario (MSS)

1. Bank teller enters a unique client reference, name, contact and card details.
2. TellerFlow creates the profile and saves it.
3. TellerFlow shows the new profile.

##### Extensions

- 1a. An optional contact detail is unavailable. Bank teller omits it; the use case resumes at step 2.
- 2a. The reference already belongs to another profile or a required value is invalid. TellerFlow explains the problem and requests a correction; the use case resumes at step 1.
- 2b. A phone number or email address matches an existing profile. TellerFlow warns the bank teller, who checks the match and chooses whether to continue creating the profile or cancel.
- 2c. Saving fails. TellerFlow reports the failure and leaves the saved profiles unchanged; the use case ends.

#### Use case: View client profiles

##### Main success scenario (MSS)

1. Bank teller requests the client list.
2. TellerFlow displays the saved profiles and their contact details.

##### Extensions

- 2a. No profiles are saved. TellerFlow displays an empty list; the use case ends.

#### Use case: Delete a client profile

##### Main success scenario (MSS)

1. Bank teller requests the client list or searches for a client.
2. TellerFlow displays matching profiles.
3. Bank teller selects a client to delete by providing the client reference number.
4. TellerFlow deletes the profile and saves the change.

##### Extensions

- 2a. The list is empty. TellerFlow shows that there is no profile to delete; the use case ends.
- 3a. The selection does not identify a displayed profile. TellerFlow reports the error; the use case resumes at step 3.
- 4a. Saving fails. TellerFlow reports the failure and retains the profile; the use case ends.

#### Use case: Reopen saved client records

##### Main success scenario (MSS)

1. Bank teller closes TellerFlow after saving client profiles.
2. Bank teller starts TellerFlow again.
3. TellerFlow loads the saved profiles.
4. Bank teller views the profiles and their contact details.

##### Extensions

- 3a. The saved data cannot be read. TellerFlow reports the problem and protects the existing data from being overwritten; the use case ends.

#### Use case: Update a client's contact details

##### Main success scenario (MSS)

1. Bank teller locates a client profile and supplies updated contact details.
2. TellerFlow updates the selected profile and saves the change.
3. TellerFlow shows the updated profile.

##### Extensions

- 1a. The selection is invalid or an updated value is invalid. TellerFlow reports the problem and requests a correction; the use case resumes at step 1.
- 2a. Saving fails. TellerFlow reports the failure and retains the previous details; the use case ends.

#### Use case: Find clients by name

##### Main success scenario (MSS)

1. Bank teller supplies `find n/` followed by one or more space-separated keywords containing all or part of a client's name.
2. TellerFlow displays profiles whose names contain at least one keyword as a case-insensitive substring.
   If `p/` numbers are also supplied, each profile must additionally match at least one full phone number.

##### Extensions

- 1a. A required prefix is missing, a field is empty, or a prefix is repeated. TellerFlow reports the invalid input
  and requests a correction; the use case resumes at step 1.
- 2a. No profile matches the supplied criteria. TellerFlow displays an empty list; the use case ends.

#### Use case: Find clients by phone number

##### Main success scenario (MSS)

1. Bank teller supplies `find p/` followed by one or more space-separated full phone numbers.
2. TellerFlow displays profiles whose phone number exactly matches any supplied number, including leading zeros.
   If `n/` keywords are also supplied, each profile must additionally match at least one name keyword.

##### Extensions

- 1a. A supplied phone number is invalid, a field is empty, or a prefix is repeated. TellerFlow reports the invalid
  input and requests a correction; the use case resumes at step 1.
- 2a. No profile matches the supplied criteria. TellerFlow displays an empty list; the use case ends.

#### Use case: Find clients by card number

##### Main success scenario (MSS)

1. Bank teller supplies a card number.
2. TellerFlow displays profiles with that recorded card number.

##### Extensions

- 2a. No card number matches. TellerFlow displays an empty list; the use case ends.

#### Use case: Maintain a service note

##### Main success scenario (MSS)

1. Bank teller locates a client profile and enters or revises a short service note.
2. TellerFlow saves the note on that profile.
3. TellerFlow shows the current note when the profile is viewed.

##### Extensions

- 1a. The selected profile does not exist. TellerFlow reports the error; the use case ends.
- 2a. Saving fails. TellerFlow reports the failure and retains the previous note; the use case ends.

#### Use case: Remove an obsolete card summary

##### Main success scenario (MSS)

1. Bank teller locates a client profile and identifies an obsolete card summary.
2. Bank teller requests its removal.
3. TellerFlow removes the summary and saves the profile.

##### Extensions

- 1a. The selected profile or card summary does not exist. TellerFlow reports the error; the use case ends.
- 3a. Saving fails. TellerFlow reports the failure and retains the card summary; the use case ends.

#### Use case: View profile timestamps

##### Main success scenario (MSS)

1. Bank teller views a client profile.
2. TellerFlow shows when the profile was created and last modified.

##### Extensions

- 1a. The selected profile does not exist. TellerFlow reports the error; the use case ends.

#### Use case: Access help

##### Main success scenario (MSS)

1. Bank teller requests help.
2. TellerFlow shows the available commands and how to use them.

#### Use case: Find clients with cards expiring in a month

##### Main success scenario (MSS)

1. Bank teller supplies an expiry month.
2. TellerFlow displays profiles with recorded cards expiring in that month.

##### Extensions

- 1a. The month is invalid. TellerFlow reports the error and requests a valid month; the use case resumes at step 1.
- 2a. No cards match. TellerFlow reports that no profiles were found; the use case ends.

#### Use case: Sort the client list by name

##### Main success scenario (MSS)

1. Bank teller requests the client list sorted by name.
2. TellerFlow displays the profiles in name order.

##### Extensions

- 2a. No profiles are saved. TellerFlow displays an empty list; the use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  If saving a change fails, both the displayed client records and the saved data file should remain in their previous valid state.
5.  If saved client data cannot be read, the application should explain the problem without overwriting the file, so the data can be repaired and loaded after a restart.
6.  Invalid input and failed operations should produce clear feedback that tells the bank teller what needs correction.
7.  Stored client and card details should be protected from unauthorized access using SHA-256 encryption, and sensitive values should not appear in logs or error messages.
8.  The system must maintain an uptime of 99.9%. During an outage or a crash, the local stored data can still be accessed and modified.
9.  The system must load all client information in under 5 seconds.

### Glossary

- **API**: The interface through which one software component provides functionality to other components.
- **Card summary**: A recorded description of a client's card that can be removed when it becomes obsolete.
- **Client profile**: A saved record containing a client's reference, name, and available contact and card details.
- **Client reference**: A unique identifier used to distinguish one client profile from another.
- **Component**: A part of the application with a distinct responsibility, such as `UI`, `Logic`, `Model`, or `Storage`.
- **Contact detail**: Information used to reach a client, such as a phone number or email address.
- **Created/last modified timestamps**: The recorded times when a profile was first created and most recently changed.
- **Exploratory testing**: Testing in which the tester investigates the application beyond the prescribed manual test cases.
- **Extension**: An alternative path from a numbered step in a use case, such as invalid input or a failed save.
- **Filtered list**: The subset of client records currently selected for display, such as search results.
- **JSON**: A text format used by `Storage` to save and load application data.
- **Main success scenario (MSS)**: The numbered steps describing how a use case normally succeeds.
- **Mainstream OS**: Windows, Linux, Unix, or macOS
- **Non-functional requirement**: A constraint on the application's quality or operation, such as performance or data integrity.
- **Observable list**: A list whose changes can be observed so the UI can update when displayed data changes.
- **Service note**: A short note on a client profile that preserves context for a later interaction.

---

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more _exploratory_ testing.
</box>

### Launch and shutdown

1. Initial launch
   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with sample clients and `No saved client data found. Showing sample clients.` The window size may not be optimal.

1. Saving window preferences
   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
      Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown
   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files
   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
