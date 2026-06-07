import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

public class SessionManager {
    private static final int TIMEOUT_MINUTES = 15;
    private static final int WARNING_MINUTES = 13;
    private Timeline timeline;
    private Runnable onTimeout;
    private boolean warningShown = false;

    public SessionManager(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    public void startSession() {
        stopSession();
        warningShown = false;
        timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            // Warning at 13 minutes
        }));

        timeline = new Timeline(
            new KeyFrame(Duration.minutes(WARNING_MINUTES), e -> {
                if (!warningShown) {
                    warningShown = true;
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                        "⚠️ 2 minute mein auto logout ho jayega!\nKoi bhi action karo session refresh karne ke liye.",
                        ButtonType.OK);
                    alert.setTitle("Session Timeout Warning");
                    alert.setHeaderText("Session Expire Hone Wali Hai!");
                    alert.show();
                }
            }),
            new KeyFrame(Duration.minutes(TIMEOUT_MINUTES), e -> {
                onTimeout.run();
            })
        );
        timeline.play();
    }

    public void resetSession() {
        warningShown = false;
        if (timeline != null) {
            timeline.stop();
            timeline.playFromStart();
        }
    }

    public void stopSession() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }
}
