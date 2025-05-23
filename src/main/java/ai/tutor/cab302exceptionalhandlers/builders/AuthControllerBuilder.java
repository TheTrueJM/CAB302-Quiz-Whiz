package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.controller.LoginController;
import ai.tutor.cab302exceptionalhandlers.controller.SignUpController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;

import java.sql.SQLException;

public class AuthControllerBuilder extends ControllerBuilder<AuthController> {
    private AuthType authType;

    public AuthControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    public AuthControllerBuilder type(AuthType type) {
        this.authType = type;
        return this;
    }

    public AuthController build() throws SQLException {
        if (authType == null) {
            throw new IllegalStateException("Auth type must be set");
        }

        return switch (authType) {
            case LOGIN -> buildLogin();
            case SIGNUP -> buildSignUp();
        };
    }

    public LoginController buildLogin() throws SQLException {
        return new LoginController(db);
    }

    public SignUpController buildSignUp() throws SQLException {
        return new SignUpController(db);
    }
}
