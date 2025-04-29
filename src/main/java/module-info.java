module ai.tutor.cab302exceptionalhandlers {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    requires password4j;
    requires ollama4j;

    opens ai.tutor.cab302exceptionalhandlers to javafx.fxml;
    opens ai.tutor.cab302exceptionalhandlers.controller to javafx.fxml, com.google.gson;

    exports ai.tutor.cab302exceptionalhandlers;
    exports ai.tutor.cab302exceptionalhandlers.model;
    exports ai.tutor.cab302exceptionalhandlers.controller;
}
