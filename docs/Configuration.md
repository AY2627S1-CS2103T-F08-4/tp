---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TellerFlow does not read a `config.json` file. Application file locations are resolved by `AppPaths`: packaged launches use the folder containing the JAR, while IDE and Gradle launches use the current working directory.

| File | Purpose |
| ---- | ------- |
| `data/addressbook.json` | Saved client profiles. |
| `data/addressbook.json.lock` | Companion file used to coordinate exclusive access to client data. Leave it in place. |
| `preferences.json` | Saved GUI settings, including window size and position. |
| `addressbook.log.0` through `addressbook.log.4` | Rotating application logs; Java may add a further numeric suffix to resolve logger conflicts. |

The data and preferences paths are selected in `MainApp` using `AppPaths.resolve`; there is no user configuration option to redirect them. Window settings are saved automatically. Missing or unreadable preferences fall back to defaults, as do JSON-null GUI settings, and the app attempts to save the recovered preferences.

Developers can change the application logging threshold using the `LOG_LEVEL` constant in `LogsCenter` and rebuilding. The default is `INFO`. See the [Logging guide](Logging.md) for details and the [User Guide](UserGuide.md#saving-the-data) for the client-data lock's lifecycle and supported usage.
