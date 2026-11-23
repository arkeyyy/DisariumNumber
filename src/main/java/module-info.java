module org.example.disarium_number {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.disarium_number to javafx.fxml;
    exports org.example.disarium_number;
}