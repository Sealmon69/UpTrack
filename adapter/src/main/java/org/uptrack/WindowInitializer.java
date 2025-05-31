package org.uptrack;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class WindowInitializer {
    private static final Logger logger = LoggerFactory.getLogger(WindowInitializer.class);

    public static final String dashboardFxml = "Dashboard.fxml";
    public static final String fxmlPath = "org/uptrack/";

    public Stage loadDashboard(String view, UserControllerService userControllerService) {
        try {
            URL url = getClass().getClassLoader().getResource(fxmlPath + view);
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(param -> {
                if (param == DashboardController.class) {
                    return new DashboardController(userControllerService);
                }
                return null; // or throw an exception if needed
            });
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("UpTrack Dashboard");
            stage.setScene(scene);
            return stage;
        } catch (IOException e) {
            logger.error("Fehler beim Laden des Dashboards", e);
            throw new RuntimeException("Dashboard konnte nicht geladen werden", e);
        }
    }
}