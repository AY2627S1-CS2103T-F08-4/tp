---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On a first run, the client list shows sample clients.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add r/C0001 n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/0.00` : Adds a client named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.
Each new client needs a unique reference (`r/REFERENCE`) and a name (`n/NAME`). References are case-insensitive and are saved in uppercase.
Phone, email, address, card details, credit score, debt, and tags are optional. Omitted details mean unknown or not recorded;
they are hidden in the client list and are saved without fabricated values. A supplied value must be valid: for example, omitting `p/` is allowed, but an empty `p/` is rejected.
Credit scores are nonnegative whole numbers. Debt is a nonnegative amount with at most two decimal places.
The debt is stored with two decimal places; no currency is assigned. An explicit `cs/0` or `d/0` records zero, which is different from an omitted value.
Card details form one optional group: supply all four of `cn/`, `cvv/`, `exp/`, and `provider/`, or omit all four.
Card number, CVV, and provider must be nonblank. Card details are saved as plain text; command input is trimmed at its edges.
The client list shows a masked card number, provider, expiry date, credit score, and debt when supplied. CVV is not displayed.
Expiry dates must use `MM/YY`, with a month from `01` to `12` (e.g. `12/28`).
This checks the format only; past expiry dates are accepted.

Format: `add r/REFERENCE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [cn/CARD_NUMBER cvv/CVV exp/EXPIRY_DATE provider/PROVIDER] [cs/CREDIT_SCORE] [d/DEBT] [t/TAG]...`

Missing phone, email, or address details can be filled using `edit`. Editing card details, credit score, or debt is follow-up work and is not currently supported. There is no syntax to clear recorded contact, card, or financial details.

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
</box>

Examples:

* `add r/C0001 n/John Doe` creates a profile with only a reference and name.
* `add r/C0002 n/Betsy Crowe e/betsycrowe@example.com cs/0` records email and a zero credit score without requiring other details.
* `add r/C0003 n/Alex Yeoh cn/4111111111111111 cvv/123 exp/12/28 provider/Visa` records a complete card group without contact or financial details.
* `add r/C0001 n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01 cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/0.00`
* `add r/C0002 n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 cn/5555555555554444 cvv/456 exp/11/29 provider/Mastercard cs/725 d/150.50 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Viewing client timestamps

Client cards show read-only `Created` and `Last Modified` times below the other client details. They appear automatically when clients are displayed after `add`, `list`, or `find`; there is no timestamp command. Times use your computer's local time in `yyyy-MM-dd HH:mm` format. For example:

```text
Created:        2026-03-15 09:42
Last Modified:  2026-10-12 14:18
```

For a newly added client, both times are the same. A successful `edit` changes `Last Modified` even when the entered values match the existing values, while `Created` stays the same. Viewing clients with `list` or `find` does not change either time. Timestamps cannot be edited through commands.

Older saved profiles may lack timestamps. If one time is available, the card shows only that time. If both are unavailable, it shows `Timestamp information is unavailable for this profile.` once. Editing an older profile adds a `Last Modified` time but does not invent a `Created` time.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Phone, email, and address can be added to incomplete profiles. Omitted fields retain their current values, including missing values. Card details, credit score, and debt cannot be edited yet.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every successful `add`, `edit`, `delete`, or `clear` command. You do not need to save manually. If saving fails, it displays `Unable to save client data. No changes were made.` and keeps the list and saved file unchanged.

On startup, saved clients appear in the main window with `Loaded [COUNT] client(s).`. If there is no data file, sample clients appear with `No saved client data found. Showing sample clients.` The sample clients are saved with the next successful data-changing command. An existing but empty data file starts with an empty list and `No saved client data found.`

### Editing the data file

AddressBook data is saved automatically as a plaintext JSON file `[JAR file location]/data/addressbook.json`. This includes client references and card details. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid or unreadable, including a malformed timestamp value, AddressBook starts with an empty list in protected mode and shows `Unable to load client data. Existing data was not changed. Add, delete, edit, and clear commands are disabled for this session.` All data-changing commands are disabled until you fix the file and restart; the existing file is not overwritten. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add r/REFERENCE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [cn/CARD_NUMBER cvv/CVV exp/EXPIRY_DATE provider/PROVIDER] [cs/CREDIT_SCORE] [d/DEBT] [t/TAG]...` <br> e.g., `add r/C0001 n/James Ho` or `add r/C0002 n/James Ho p/22224444 e/jamesho@example.com t/friend`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**Help**   | `help`
