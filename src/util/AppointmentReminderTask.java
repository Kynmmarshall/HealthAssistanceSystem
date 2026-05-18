package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.AppointmentDAO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.Appointment;

public class AppointmentReminderTask implements Runnable {
    private volatile boolean running = true;
    private final Set<Integer> notifiedAppointmentIds = new HashSet<>();
    private volatile boolean stopSoundRequested = false;
    private Thread soundThread = null;
    private MediaPlayer mediaPlayer = null;

    @Override
    public void run() {
        while (running) {
            try {
                int expiredCount = AppointmentDAO.markExpiredAppointments();
                if (expiredCount > 0) {
                    System.out.println("[REMINDER DEBUG] Marked " + expiredCount + " appointments as Expired");
                }

                List<Appointment> upcoming = AppointmentDAO.getUpcomingAppointments();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                
                System.out.println("[REMINDER DEBUG] Current time: " + now.format(formatter) + " | Found " + upcoming.size() + " upcoming appointments");
                
                for (Appointment a : upcoming) {
                    LocalDateTime appointmentTime = a.getAppointmentDate();
                    long minutesDiff = java.time.temporal.ChronoUnit.MINUTES.between(now, appointmentTime);
                    
                    System.out.println("[REMINDER DEBUG] Appointment ID: " + a.getId() + " | Time: " + appointmentTime.format(formatter) + " | Minutes until: " + minutesDiff);
                    
                    // Trigger notification when appointment is within 5 minutes before or after appointment time
                    // This gives a wider window for catching the appointment
                        boolean withinWindow = !appointmentTime.isAfter(now.plusMinutes(5))
                            && !appointmentTime.isBefore(now.minusMinutes(5));
                        if (!notifiedAppointmentIds.contains(a.getId()) && withinWindow) {
                        
                        System.out.println("[REMINDER DEBUG] TRIGGERING ALERT for appointment ID: " + a.getId());
                        notifiedAppointmentIds.add(a.getId());
                        
                        // Start sound in background thread
                        stopSoundRequested = false;
                        soundThread = new Thread(() -> playAlertSound());
                        soundThread.setDaemon(true);
                        soundThread.start();
                        System.out.println("[REMINDER DEBUG] Sound thread started, waiting 100ms for FX thread...");
                        Thread.sleep(100); // Give FX thread time to be ready
                        
                        // Show notification with Stop button
                        Platform.runLater(() -> {
                            try {
                                System.out.println("[REMINDER DEBUG] Creating alert dialog...");
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Appointment Reminder");
                                alert.setHeaderText("Your appointment is NOW!");
                                alert.setContentText("Date & Time: " + a.getAppointmentDate().format(formatter) 
                                        + "\n\nClick 'Stop' to dismiss this alert and stop the sound.");
                                
                                // Replace default button with custom "Stop" button
                                alert.getButtonTypes().clear();
                                alert.getButtonTypes().add(new ButtonType("Stop"));
                                
                                // Ensure alert is on top and has focus
                                alert.setResizable(true);
                                alert.setWidth(400);
                                alert.setHeight(250);
                                
                                System.out.println("[REMINDER DEBUG] About to show alert - calling showAndWait()...");
                                alert.showAndWait();
                                System.out.println("[REMINDER DEBUG] Alert was dismissed");
                                
                                // Stop the sound when alert is dismissed
                                stopSoundRequested = true;
                            } catch (Exception e) {
                                System.err.println("[REMINDER ERROR] Failed to show alert: " + e.getMessage());
                                e.printStackTrace();
                                stopSoundRequested = true;
                            }
                        });
                    }
                }
                Thread.sleep(30000); // check every 5 seconds for better responsiveness
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Reminder task error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Plays MP3 alert sound in a loop until stopSoundRequested is set to true.
     * Falls back to system beep if MP3 playback is unavailable.
     * Loads audio file from resources/audio/alert.mp3
     */
    private void playAlertSound() {
        boolean mp3Failed = false;
        
        try {
            // First check if the resource exists
            java.net.URL audioResource = getClass().getResource("/resources/audio/alert.mp3");
            if (audioResource == null) {
                throw new NullPointerException("Resource /resources/audio/alert.mp3 not found in classpath");
            }
            
            String audioPath = audioResource.toExternalForm();
            System.out.println("[REMINDER DEBUG] Attempting to load MP3 from: " + audioPath);
            System.out.println("[REMINDER DEBUG] File protocol: " + audioResource.getProtocol());
            
            Media media = new Media(audioPath);
            mediaPlayer = new MediaPlayer(media);
            
            // Set to loop indefinitely
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            
            // Set explicit volume
            mediaPlayer.setVolume(1.0);
            System.out.println("[REMINDER DEBUG] MediaPlayer volume set to: " + mediaPlayer.getVolume());
            
            // Add error handler
            mediaPlayer.setOnError(() -> {
                System.err.println("[REMINDER ERROR] MediaPlayer error: " + mediaPlayer.getError().getMessage());
                mediaPlayer.getError().printStackTrace();
            });
            
            // Wait for media to be ready before playing
            mediaPlayer.setOnReady(() -> {
                System.out.println("[REMINDER DEBUG] Media ready, duration: " + media.getDuration());
            });
            
            // Start playing
            mediaPlayer.play();
            System.out.println("[REMINDER DEBUG] MP3 playback started successfully. Status: " + mediaPlayer.getStatus());
            
            // Keep thread alive while sound is playing, check stop signal
            // Loop until stop is requested (don't depend on playing status which can be unreliable)
            while (!stopSoundRequested) {
                Thread.sleep(100);
            }
            
            // Stop and clean up
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                System.out.println("[REMINDER DEBUG] MP3 playback stopped");
            }
        } catch (NullPointerException e) {
            // Audio file not found
            mp3Failed = true;
            System.err.println("[REMINDER ERROR] MP3 file not found!");
            System.err.println("[REMINDER ERROR] Expected location: src/resources/audio/alert.mp3");
            System.err.println("[REMINDER ERROR] Runtime classpath location: /resources/audio/alert.mp3");
            System.err.println("[REMINDER ERROR] Make sure the file exists and is in the correct location.");
            System.err.println("[REMINDER INFO] Falling back to system beep...");
        } catch (IllegalAccessError e) {
            // Module system issue - needs JVM args to fix
            mp3Failed = true;
            System.err.println("[REMINDER WARNING] MP3 playback requires module configuration.");
            System.err.println("[REMINDER INFO] Add this JVM argument: --add-exports javafx.base/com.sun.javafx=ALL-UNNAMED");
            System.err.println("[REMINDER INFO] Full command example:");
            System.err.println("[REMINDER INFO]   java --add-exports javafx.base/com.sun.javafx=ALL-UNNAMED -cp bin application.Main");
            System.err.println("[REMINDER INFO] Falling back to system beep for now...");
        } catch (Exception e) {
            // Other playback errors
            mp3Failed = true;
            System.err.println("[REMINDER ERROR] Failed to play MP3: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            System.err.println("[REMINDER INFO] Falling back to system beep...");
        }
        
        // Fallback to system beep if MP3 failed
        if (mp3Failed) {
            playSystemBeep();
        }
    }

    /**
     * Fallback: plays system beep repeatedly until stop is requested.
     */
    private void playSystemBeep() {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            System.out.println("[REMINDER DEBUG] Starting system beep loop...");
            int beepCount = 0;
            while (!stopSoundRequested) {
                toolkit.beep();
                beepCount++;
                System.out.println("[REMINDER DEBUG] Beep #" + beepCount);
                Thread.sleep(500);
            }
            System.out.println("[REMINDER DEBUG] Beep loop stopped after " + beepCount + " beeps");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
        stopSoundRequested = true;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}