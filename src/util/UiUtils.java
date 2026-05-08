package util;

import javafx.scene.Node;
import javafx.stage.Stage;

public final class UiUtils {

    private UiUtils() {
    }

    public static void toggleFullscreen(Node node) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return;
        }

        Stage stage = (Stage) node.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    public static void closeWindow(Node node) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return;
        }

        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
}