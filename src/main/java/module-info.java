module ai.tutor.cab302exceptionalhandlers {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive java.sql;
    requires password4j;

    opens ai.tutor.cab302exceptionalhandlers to javafx.fxml;
    opens ai.tutor.cab302exceptionalhandlers.controller to javafx.fxml;

    exports ai.tutor.cab302exceptionalhandlers;
    exports ai.tutor.cab302exceptionalhandlers.model;
    exports ai.tutor.cab302exceptionalhandlers.controller;
}
