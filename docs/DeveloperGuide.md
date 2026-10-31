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

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="1100" alt="Delete command stages a model change, saves it, and commits only after persistence succeeds" />

Each of the four main components (also shown in the diagram above),

- defines its _API_ in an `interface` with the same name as the Component.
- provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonDetailsPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts. `MainWindow` connects list selection to `PersonDetailsPanel`, which shows the selected client's details or an empty state when no client is selected.

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

<puml src="diagrams/DeleteSequenceDiagram.puml" width="1200" alt="Delete execution resolves a displayed index to a Person in a staged model, saves through AddressBookSync, then commits or reports failure" />

<a href="diagrams/DeleteSequenceDiagram.png" no-validation>Open the full-size delete sequence diagram</a> to inspect the calls and failure path.

**Diagram note:** The `DeleteCommandParser` lifeline should end at the destroy marker (X); PlantUML may continue its dashed line below that marker.

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (an instance of a subclass such as `DeleteCommand`). `LogicManager` checks whether it changes client data. Protected mode rejects data-changing commands before they execute.
1. `Model#beginStaging` returns a `ModelStage`, whose `model()` supplies the isolated model used by a data-changing command. For `delete 1`, the command resolves the first displayed client from that staged model and passes the `Person` to `deletePerson`; the active model is unchanged at this point.
1. Unless the command requests clash confirmation, `LogicManager` saves the staged address book through `AddressBookSync` and `Storage`. After saving succeeds, `ModelStage#commit` publishes the staged data and filter to the active model. If saving fails, no commit occurs and logic throws `CommandException` with `Unable to save client data. No changes were made.`
1. The command's `CommandResult` is returned after a successful commit. Commands that do not change client data execute directly against the active model without staging or saving.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:

- When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
- All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component

**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F08-4/tp/blob/master/src/main/java/seedu/address/model/Model.java)

The following diagram shows the model classes, their links to the stored and displayed clients, and each client's value objects. `Person` connects the model overview to its profile fields in the same diagram.

<puml src="diagrams/ModelClassDiagram.puml" width="1200" alt="Model class diagram connecting ModelManager and UniquePersonList to Person, and Person to its profile value classes" />

<!-- The PNG is generated by the puml tag during the site build. -->
<a href="diagrams/ModelClassDiagram.png" no-validation>Open the full-size model diagram</a> to inspect the class fields and multiplicities.

The `Model` component,

- stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
- maintains a `FilteredList<Person>` for the current filter, such as search results, and a `SortedList<Person>` over that filtered view. `ModelManager#getFilteredPersonList()` exposes the sorted view as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to.
- stores the current session's `SortOrder` (`ORIGINAL`, `ASC`, or `DESC`). Changing it changes the displayed order without rearranging the stored `UniquePersonList` or saving the order to disk.
- stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
- does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

#### Client profile fields

`Person` is an immutable client profile. In the model diagram above, its custom value types appear as classes connected by named associations. Each value class shows its stored fields; `CardDetails` groups four required strings. These references are represented only by associations, while the two `Instant` timestamps remain attributes of `Person`.

An association or attribute multiplicity of `0..1` means the stored field may be `null`; `name` is required. The `tags` association has multiplicity `0..*` and is unique because tags are stored in a non-null set that can be empty. All getters for nullable fields return `Optional` values. Ordinary associations allow immutable value objects to be shared when a profile is copied; the diagram does not assert exclusive ownership through composition.

New clients created through `add` require a `ClientReference`. Unreferenced legacy profiles remain supported. References determine client identity when present; profiles without references retain name-based identity. Contact, card, credit score, and debt fields may be absent independently. If a card is present, its `cardNumber`, `cvv`, `expiryDate`, and `provider` fields are required strings.

`createdAt` and `lastModifiedAt` use `java.time.Instant`. `ModelManager#addPerson` supplies both timestamps, while `ModelManager#setPerson` preserves the creation time and updates the modification time using its clock. Older profiles may lack either timestamp. See [Client creation and modification timestamps](#client-creation-and-modification-timestamps) for the persistence and display behavior.

<box type="info" seamless>

**Alternative design (not implemented):** The diagram below changes only tag ownership: `AddressBook` keeps a unique list of tags, and each `Person` references tags from that list. The other `Person` fields remain as shown above and are omitted here. In the current implementation, each `Person` holds its own set of tags and there is no `UniqueTagList` class.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="500" alt="Unimplemented alternative in which AddressBook owns a shared UniqueTagList" />
</box>

### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

- can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
- is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
- uses `JsonSerializableAddressBook` and `JsonAdaptedPerson` to convert saved client records to and from model objects. Each adapted person can contain optional `JsonAdaptedCardDetails` and a collection of `JsonAdaptedTag` objects.
- depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

---

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Exclusive access to client data

`MainApp#isStorageInitialized` acquires an `AddressBookFileLock` before reading or writing preferences or loading client data. `AddressBookFileLock#tryAcquire` normalizes the absolute data path, creates its parent directory if needed, and opens the companion `addressbook.json.lock` file using `CREATE` and `WRITE`. `FileChannel#tryLock()` obtains an exclusive operating-system lock; the existence of the companion file alone does not block startup.

The channel and lock remain open for the application's lifetime. `MainApp#stop` releases them in a `finally` block after attempting to save preferences; initialization failures also release the lock when caught. Process termination releases the operating-system lock, so a crash does not require manual cleanup. `AddressBookFileLock#close` deliberately retains the companion file to avoid races between instances opening and locking it.

If another instance holds the lock, startup displays `The client data is already open in another TellerFlow instance. Close that instance and try again.` If establishing the lock throws an `IOException`, startup instead asks the user to check that the data folder is writable. Both paths show a dialog and exit without initializing storage, the model, or the main window. They do not enter protected mode.

Deleting, renaming, or replacing an active companion file is unsupported. On filesystems that allow removal of an open file, the first process can retain its lock on the original file while another process creates and locks a new file at the same path. The two instances can then overwrite the same client data. Behaviour after such interference is undefined by TellerFlow; resulting lost updates are outside supported usage and should not be reported as application bugs. This companion file is separate from Java logging's `.lck` files and does not prevent an external editor from modifying `addressbook.json`.

`AddressBookFileLockTest` covers exclusion within the JVM and across processes, reacquisition after close, and recovery after forced process termination with the companion file retained. `MainAppInstanceLockTest` covers startup rejection, unchanged client data and preferences, initialization cleanup, and the second-instance error dialog.

### Client creation and modification timestamps

`Person` stores `createdAt` and `lastModifiedAt` as `Instant` values. `ModelManager#addPerson` stamps both with the same clock instant. `ModelManager#setPerson` copies the existing `createdAt` and stamps a new `lastModifiedAt`; this also applies when a valid edit supplies unchanged values. Read-only commands such as `list` and `find` do not stamp records. These fields are not part of command input or client uniqueness checks.

Data-changing commands execute against a staged `ModelManager`. `LogicManager` saves the staged address book before committing it to the active model. After a successful add, it emits the INFO message `Timestamp ADD recorded`. Successful `edit`, `note`, and `removecard` commands emit `Timestamp EDIT recorded` because they use `ModelManager#setPerson`. These messages contain neither client references nor timestamp values. A failed save leaves the active record unchanged and emits no timestamp-change log entry.

`JsonAdaptedPerson` writes the instants as ISO-8601 UTC strings in the `createdAt` and `lastModifiedAt` JSON properties. Missing properties in older data files remain `null`; editing such a record supplies its modification time without fabricating a creation time. A malformed timestamp that `Instant.parse` cannot parse causes loading to fail, so startup follows the protected-mode path.

`PersonDetailsPanel` converts displayable instants to the viewer's local time zone and formats them as `yyyy-MM-dd HH:mm`. `TimestampUtil` defines the inclusive displayable range as `0001-01-01T18:00:00Z` through `9999-12-31T05:59:59.999999999Z`, keeping local dates within years 0001–9999 for offsets from −18:00 to +18:00. A parseable instant outside this range is preserved in the model and saved data; only its displayed field shows `Timestamp information is invalid for this profile.` It does not cause protected mode or hide another valid timestamp. Each missing timestamp is hidden independently, with one unavailable message if both are missing.

### Confirmation of shared client details

`AddCommand` and `EditCommand` first reject duplicate client identities, then call `ModelManager#findClientClashes`. It compares the proposed profile with the full stored client list, excluding the client being edited. Phone and card numbers use exact string equality. `Email#isSameMailboxAs` compares local parts case-sensitively and domains case-insensitively. Absent values do not clash; a retained value can still clash with another client during an edit.

If clashes exist and confirmation was not supplied, the command returns `CommandResult.requestClashConfirmation` before changing the staged model. `LogicManager` attaches the original command text and returns without saving or committing. `MainWindow` retains that command and accepts typed `yes` or `no`, ignoring case and surrounding spaces. Other command-box input leaves the request pending. `no` cancels; `yes` retries the original command with `confirm/y` appended. The warning masks card numbers, except numbers of four characters or fewer.

Both command parsers also accept `confirm/y` directly, at most once; the value `y` is case-insensitive and other values cause a parse error. Confirmation bypasses only the shared-detail warning. It does not bypass identity or field validation, the requirement to provide an editable field, protected mode, or persistence errors.

### Incomplete client profiles

`add` requires only a valid, unique client reference and name. References are normalized to uppercase and compared case-insensitively. Its syntax is:

```text
add r/REFERENCE n/NAME [p/PHONE] [e/EMAIL] [a/ADDRESS] [cn/CARD_NUMBER cvv/CVV exp/EXPIRY_DATE provider/PROVIDER] [cs/CREDIT_SCORE] [d/DEBT] [t/TAG]... [confirm/y]
```

`AddCommandParser` parses supplied optional fields using the existing validators and passes null for omitted details. Supplying an empty prefix still fails validation. Card details form one optional group: any non-empty proper subset of the four card prefixes is rejected with a message listing the required group. Duplicate single-valued prefixes and non-empty preambles are rejected as before.

`Person` stores missing contact, card, and financial details as nullable fields and exposes them through non-null `Optional` getters. Each detail can be supplied independently, including a credit score or debt without a card. A supplied zero financial value remains distinct from absence. Required name and tag collections remain non-null; the referenced constructor also requires a reference. Unreferenced legacy records retain their existing name-based identity.

`PersonDetailsPanel` hides absent rows and empty contact or financial sections, removing their layout space. Messages omit missing contact fields and their labels. The details panel masks all but the last four characters of a card number (numbers of four characters or fewer are shown in full) and displays the provider, full CVV, and expiry date. `JsonAdaptedPerson` serializes plain values, omits absent optional fields, and accepts missing or JSON-null optional details. A present card object must contain all four valid fields. Complete profiles and supported legacy files continue to load.

For example, `add r/C0001 n/John Doe` creates a minimal profile, while `add r/C0002 n/Jane Doe e/jane@example.com d/0` records an email and explicit zero debt. Data-changing commands save automatically. `edit` can fill or update contact details, card details, credit score, and debt while retaining omitted fields. It can also change the reference and update tags or the service note.

`EditCommand#mergeCardDetails` merges individual supplied card fields with an existing card. If there is no card, all four fields must be supplied together; an incomplete group causes a command error. Credit score and debt can be edited independently, whether or not the client has a card. For example, `edit 1 cs/725 d/150.50` updates both financial values, and `edit 1 exp/12/29` updates only the expiry date of an existing card.

Empty contact, card, credit score, or debt values are rejected. Commands cannot clear contact or financial details. `removecard` clears the complete card group while preserving other details and service notes; `edit` can clear tags with `t/` or a service note with `s/`.

Regression tests cover all 64 combinations of optional details, all 14 non-empty proper subsets of card prefixes, JSON file round trips, and add followed by automatic save, fresh load, contact edit, and reload. Existing duplicate detection and storage-failure protections remain in place.

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

### Remove card

`removecard r/REFERENCE` parses and normalizes the client reference, then locates the client in the full address book. `RemoveCardCommand` replaces that client with a copy whose card details are absent, preserving the reference, contact details, financial details, and tags. A referenced client without a card remains valid when loaded from JSON.

The command is data-changing, so `LogicManager` runs it on a staged model. Storage writes the new address book to a temporary file and atomically replaces the saved file; only then does logic publish the staged model. The JSON adapter omits the null `cardDetails` field. A failed lookup, missing card, protected mode, or save failure leaves the displayed and saved data unchanged. On success, the client row refreshes and a selected client remains selected.

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

1. Bank teller enters a unique client reference, name, and any available optional details.
2. TellerFlow creates the profile and saves it.
3. TellerFlow shows the new profile.

##### Extensions

- 1a. An optional contact, card, or financial detail is unavailable. Bank teller omits it; card details must be omitted as a group. The use case resumes at step 2.
- 2a. The reference already belongs to another profile or a required value is invalid. TellerFlow explains the problem and requests a correction; the use case resumes at step 1.
- 2b. A phone number, email address, or card number matches an existing profile. Unless `confirm/y` was supplied, TellerFlow warns the bank teller before saving. The bank teller types `yes` to proceed or `no` to cancel; other command-box input leaves the confirmation pending. The use case resumes at step 2 after confirmation or ends after cancellation.
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
3. Bank teller selects a client to delete by providing the index or client reference number.
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
- 2a. The resulting phone number, email address, or card number matches another profile. Unless `confirm/y` was supplied, TellerFlow requests typed `yes` or `no` confirmation before saving. The use case resumes at step 2 after confirmation or ends after cancellation.
- 2b. Saving fails. TellerFlow reports the failure and retains the previous details; the use case ends.

#### Use case: Find clients by name

##### Main success scenario (MSS)

1. Bank teller supplies `find n/` followed by one or more space-separated keywords containing all or part of a client's name.
2. TellerFlow displays profiles whose names contain at least one keyword as a case-insensitive substring.
   If `p/` numbers are also supplied, each profile must additionally match at least one full phone number.
   If `cn/` is supplied, each profile must additionally match the full recorded card number.

##### Extensions

- 1a. A required prefix is missing, a field is empty, or a prefix is repeated. TellerFlow reports the invalid input
  and requests a correction; the use case resumes at step 1.
- 2a. No profile matches the supplied criteria. TellerFlow displays an empty list; the use case ends.

#### Use case: Find clients by phone number

##### Main success scenario (MSS)

1. Bank teller supplies `find p/` followed by one or more space-separated full phone numbers.
2. TellerFlow displays profiles whose phone number exactly matches any supplied number, including leading zeros.
   If `n/` keywords are also supplied, each profile must additionally match at least one name keyword.
   If `cn/` is supplied, each profile must additionally match the full recorded card number.

##### Extensions

- 1a. A supplied phone number is invalid, a field is empty, or a prefix is repeated. TellerFlow reports the invalid
  input and requests a correction; the use case resumes at step 1.
- 2a. No profile matches the supplied criteria. TellerFlow displays an empty list; the use case ends.

#### Use case: Find clients by card number

##### Main success scenario (MSS)

1. Bank teller supplies `find cn/` followed by one full card number.
2. TellerFlow displays profiles with exactly that recorded card number, preserving leading zeros, internal spaces
   and hyphens. Leading and trailing whitespace is ignored. Profiles without card details do not match.
   If `n/` and/or `p/` are also supplied, each profile must additionally match each supplied field.

##### Extensions

- 1a. The card number is blank or a prefix is repeated or unsupported. TellerFlow reports the invalid input
  and requests a correction; the use case resumes at step 1.
- 2a. No card number matches. TellerFlow displays an empty list; the use case ends.

#### Use case: Maintain a service note

##### Main success scenario (MSS)

1. Bank teller locates a client profile and enters or revises a short service note.
2. TellerFlow saves the note on that profile.
3. TellerFlow shows the current note in the last section of the selected profile's details panel.

##### Extensions

- 1a. The selected profile does not exist. TellerFlow reports the error; the use case ends.
- 2a. Saving fails. TellerFlow reports the failure and retains the previous note; the use case ends.

#### Use case: Remove an obsolete card summary

##### Main success scenario (MSS)

1. Bank teller identifies a client reference and enters `removecard r/REFERENCE`.
2. TellerFlow finds the client and its card summary.
3. TellerFlow removes the summary and saves the profile without `cardDetails`.
4. TellerFlow reports the last four characters of the removed card number and refreshes the client row. The client remains selected if previously selected.

##### Extensions

- 1a. The command has a missing, repeated, or extra parameter. TellerFlow reports the input error; the use case ends.
- 2a. The client reference does not exist or the client has no card. TellerFlow reports the error; the use case ends.
- 3a. Saving fails. TellerFlow reports the failure and retains the card summary in memory and on disk; the use case ends.
- 3b. TellerFlow is in protected mode after a load failure. It rejects the command without changing the data; the use case ends.

#### Use case: View profile timestamps

##### Main success scenario (MSS)

1. Bank teller adds a client or displays clients with `list` or `find`, then selects a client.
2. TellerFlow shows the available creation and last-modified times in the selected client's details panel, without changing either time.

##### Extensions

- 2a. Only one time is available. TellerFlow shows that time and hides the unavailable field.
- 2b. Neither time is available. TellerFlow shows `Timestamp information is unavailable for this profile.` once.

#### Use case: Access help

##### Main success scenario (MSS)

1. Bank teller requests help.
2. TellerFlow opens the Help window, displaying the URL of this repository's TellerFlow User Guide and a Copy URL button.
3. Bank teller copies the URL and opens it in a browser to read the available commands and their usage.

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
7.  Sensitive command input should not be written to application logs. **Future requirement (not implemented):** encrypt stored client and card details at rest to protect their confidentiality. The current release stores plaintext JSON; SHA-256 is a hash function, not an encryption mechanism.
8.  The system must maintain an uptime of 99.9%. During an outage or a crash, the local stored data can still be accessed and modified.
9.  The system must load all client information in under 5 seconds.

The client-data lock prevents cooperating TellerFlow instances from saving to the same data file concurrently. It provides no encryption or user authentication. Card-number masking affects presentation only; the complete recorded card details, including CVV, remain in the saved JSON.

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
      Expected: The most recent window size and location are restored within the available screen bounds. Oversized or off-screen saved settings are adjusted to an available screen.

   1. With two monitors connected, move the window to the secondary monitor and close the app. Disconnect that monitor and relaunch.<br>
      Expected: The window opens within the primary screen's visual bounds without deleting preferences.

1. _{ more test cases … }_

### Client data locking

Use a disposable folder containing the packaged JAR for these tests. Keep `data/addressbook.json.lock` in place throughout normal launch, shutdown, and crash recovery.

1. Start TellerFlow, add `add r/LOCKTEST n/Lock Test`, and record the saved client data and preferences. Launch the same JAR again while the first instance remains open.<br>
   Expected: The second launch shows the already-open message and closes after the dialog is dismissed. Client data and preferences are unchanged by the second launch; the first instance can still save changes.

1. Close the first instance normally and launch again.<br>
   Expected: The companion file remains, the launch succeeds, and the saved clients load.

1. Force-terminate the test application's process, then launch again without deleting the companion file.<br>
   Expected: The launch succeeds and the last successfully saved clients load. Do not perform this test against real client data.

1. Close all instances. In a separate disposable folder, create a directory named `data/addressbook.json.lock` before the first launch.<br>
   Expected: Startup shows the could-not-secure-access message and exits without creating client data or preferences. With all instances closed, remove the test directory and launch again; startup succeeds.

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

### Removing a card

1. Start with a data file that has no `C0001` client. Add a client using `add r/C0001 n/Alex Yeoh p/87438807 e/alex@example.com a/10 Main Street cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50`.

   1. Select the new client row and run `removecard r/c0001`.<br>
      Expected: `Removed card ending in 1111 from client C0001`. The row remains selected and no longer shows a card summary. The client and other details remain. After restart, the saved JSON has no `cardDetails` field for C0001.

   1. Run `removecard r/C0001` again.<br>
      Expected: `Client C0001 is not associated with a card.` The data stays unchanged.

   1. Run `removecard r/C9999`, `removecard`, and `removecard r/C0001 r/C0002`.<br>
      Expected: The client-not-found, missing-reference, and repeated-parameter messages respectively; no data changes.

1. With an invalid saved data file, restart the app and run `removecard r/C0001`.<br>
   Expected: The protected-mode message appears and the file is not overwritten.

### Saving data

1. Viewing client timestamps
   1. Add a client using the example `add` command in the User Guide.<br>
      Expected: Select the client. Its details panel shows equal `Created` and `Last Modified` times in local `yyyy-MM-dd HH:mm` format.

   1. Run `list`, then `find n/John` for that client.<br>
      Expected: Both times remain unchanged in the selected client's details panel.

   1. After the clock reaches the next minute, run `edit 1 p/91234567` while the client is the first search result.<br>
      Expected: `Created` remains unchanged and `Last Modified` reflects the successful edit. The corresponding INFO message is `Timestamp EDIT recorded`, without a client reference or stored instants.

   1. Relaunch the app.<br>
      Expected: The saved times are restored and shown in local time.

   1. Close the app, back up the saved data file, remove both timestamp properties from the test client's entry, and relaunch.<br>
      Expected: The selected client's details panel shows `Timestamp information is unavailable for this profile.` once. If only one property is removed, it shows only the remaining time.

   1. Close the app and restore the backup. Replace only `createdAt` with the parseable instant `+10000-01-01T00:00:00Z`, then relaunch.<br>
      Expected: The app loads normally. `Created` shows `Timestamp information is invalid for this profile.` and `Last Modified` displays normally. Run `note` for the test client and inspect the file after closing: the original out-of-range `createdAt` value is preserved. Restore the backup with the app closed.

   1. With the app closed, replace one timestamp in the saved data file with invalid text and relaunch.<br>
      Expected: The app enters protected mode and disables `add`, `delete`, `edit`, and `clear` for the session without overwriting the file. Restore the backup after this test.

1. Dealing with missing/corrupted data files
   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
