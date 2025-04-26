module ai.tutor.cab302exceptionalhandlers {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires password4j;

    opens ai.tutor.cab302exceptionalhandlers to javafx.fxml;
    exports ai.tutor.cab302exceptionalhandlers;
    exports ai.tutor.cab302exceptionalhandlers.controller;
    opens ai.tutor.cab302exceptionalhandlers.controller to javafx.fxml;
}
