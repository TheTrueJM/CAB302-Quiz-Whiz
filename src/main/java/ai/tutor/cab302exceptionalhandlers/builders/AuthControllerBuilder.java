package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.controller.LoginController;
import ai.tutor.cab302exceptionalhandlers.controller.SignUpController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;

import java.sql.SQLException;

/**
 * Builder for creating instances of {@link AuthController}.
 * <p>
 * This class constructs either a {@link LoginController} or a {@link SignUpController}
 * based on the specified {@link AuthType}.
 *
 * <p>Usage Example:
 * <pre>
 * AuthController controller = controllerFactory.authController()
 *                                .type(AuthType.LOGIN) // or AuthType.SIGNUP
 *                                .build();
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.LoginController
 * @see ai.tutor.cab302exceptionalhandlers.controller.SignUpController
 */
public class AuthControllerBuilder extends ControllerBuilder<AuthController> {
    private AuthType authType;

    /**
     * Constructs an {@code AuthControllerBuilder}.
     *
     * @param db The {@link SQLiteConnection} to be used for database operations.
     * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
     */
    public AuthControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    /**
     * Sets the type of authentication controller to build (LOGIN or SIGNUP).
     *
     * @param type The {@link AuthType} specifying the controller type.
     * @return This {@code AuthControllerBuilder} instance for chaining.
     */
    public AuthControllerBuilder type(AuthType type) {
        this.authType = type;
        return this;
    }

    /**
     * Builds an {@link AuthController} (either {@link LoginController} or {@link SignUpController})
     * based on the specified {@link AuthType}.
     *
     * @return An instance of {@link AuthController}.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if the authentication type is not set.
     */
    public AuthController build() throws SQLException {
        if (authType == null) {
            throw new IllegalStateException("Auth type must be set");
        }

        return switch (authType) {
            case LOGIN -> buildLogin();
            case SIGNUP -> buildSignUp();
        };
    }

    /**
     * Builds a {@link LoginController}.
     *
     * @return A new instance of {@link LoginController}.
     * @throws SQLException if a database access error occurs.
     */
    public LoginController buildLogin() throws SQLException {
        return new LoginController(db);
    }

    /**
     * Builds a {@link SignUpController}.
     *
     * @return A new instance of {@link SignUpController}.
     * @throws SQLException if a database access error occurs.
     */
    public SignUpController buildSignUp() throws SQLException {
        return new SignUpController(db);
    }
}
