package seedu.address;

import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Launches a real JavaFX application to exercise the blocked-startup dialog in a separate process. */
public class StartupDialogProbe {
    public static void main(String[] args) {
        Application.launch(BlockedApp.class, args);
    }

    /** Attempts startup while the parent test process holds the data lock. */
    public static class BlockedApp extends MainApp {
        @Override
        public void init() {
            Path dataPath = Path.of(getParameters().getRaw().getFirst());
            if (initStorage(dataPath.resolveSibling("prefs.json"), dataPath)) {
                throw new IllegalStateException("Second instance unexpectedly acquired the data lock");
            }
        }

        @Override
        public void start(Stage primaryStage) {
            Platform.runLater(() -> {
                try {
                    Window dialog = Window.getWindows().stream().filter(Window::isShowing)
                            .filter(window -> window != primaryStage).findFirst().orElseThrow();
                    DialogPane pane = (DialogPane) dialog.getScene().getRoot();
                    if (!pane.getContentText().contains("already open in another TellerFlow instance")) {
                        throw new IllegalStateException("Expected a concurrent-instance error dialog");
                    }
                    if (pane.getHeaderText() != null || pane.getGraphic() != null
                            || !pane.getButtonTypes().equals(List.of(ButtonType.OK))) {
                        throw new IllegalStateException("Expected only an error message and an OK button");
                    }
                    Button okButton = (Button) pane.lookupButton(ButtonType.OK);
                    okButton.fire();
                    System.out.println("STARTUP_DIALOG_SHOWN");
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    Platform.exit();
                }
            });
            super.start(primaryStage);
        }
    }
}
