package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.AppointmentReminderTask;

public class Main extends Application {

    private static Thread reminderThread;
    private static AppointmentReminderTask reminderTask;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/login.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
        primaryStage.initStyle(StageStyle.UNDECORATED); // optional: remove OS title bar
        primaryStage.setTitle("Health Assistance System");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start background reminder thread
        reminderTask = new AppointmentReminderTask();
        reminderThread = new Thread(reminderTask);
        reminderThread.setDaemon(true);
        reminderThread.start();
    }

    @Override
    public void stop() {
        if (reminderTask != null) {
            reminderTask.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}