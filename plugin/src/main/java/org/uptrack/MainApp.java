package org.uptrack;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        System.out.println("Hello, UpTrack!");
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        UserControllerService userControllerService = new UserControllerService();
        WindowInitializer windowInitializer = new WindowInitializer();
        Stage mainStage = windowInitializer.loadDashboard(WindowInitializer.dashboardFxml, userControllerService);
        mainStage.show();
        System.out.println("UpTrack application started successfully.");
    }
}

