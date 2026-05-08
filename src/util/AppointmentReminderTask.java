package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.AppointmentDAO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import model.Appointment;

public class AppointmentReminderTask implements Runnable {
    private volatile boolean running = true;
    private final Set<Integer> notifiedAppointmentIds = new HashSet<>();

    @Override
    public void run() {
        while (running) {
            try {
                List<Appointment> upcoming = AppointmentDAO.getUpcomingAppointments();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (Appointment a : upcoming) {
                    if (!notifiedAppointmentIds.contains(a.getId())
                            && a.getAppointmentDate().isBefore(now.plusMinutes(30))
                            && a.getAppointmentDate().isAfter(now)) {
                        notifiedAppointmentIds.add(a.getId());
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Appointment Reminder");
                            alert.setHeaderText("You have an appointment soon!");
                            alert.setContentText("Date & Time: " + a.getAppointmentDate().format(formatter));
                            alert.showAndWait();
                        });
                        // Remove after reminder? For simplicity we notify once.
                    }
                }
                Thread.sleep(60000); // check every minute
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Reminder task error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
    }
}