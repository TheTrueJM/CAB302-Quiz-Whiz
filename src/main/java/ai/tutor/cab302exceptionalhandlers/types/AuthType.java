package ai.tutor.cab302exceptionalhandlers.types;

/**
 * Represents the type of authentication process.
 * Used to determine whether to display a login or sign-up interface.
 *
 * @author Justin
 * @see ai.tutor.cab302exceptionalhandlers.builders.AuthControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.AuthController
 * @see ai.tutor.cab302exceptionalhandlers.controller.LoginController
 * @see ai.tutor.cab302exceptionalhandlers.controller.SignUpController
 */
public enum AuthType {
    LOGIN,
    SIGNUP
}
