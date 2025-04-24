module org.sebamuel.uptrack {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.sebamuel.uptrack to javafx.fxml;
    exports org.sebamuel.uptrack;
}