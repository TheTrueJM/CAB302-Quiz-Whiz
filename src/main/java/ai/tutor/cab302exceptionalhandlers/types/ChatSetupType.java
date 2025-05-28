package ai.tutor.cab302exceptionalhandlers.types;

/**
 * Represents the type of chat setup operation.
 * Used to determine whether to configure settings for a new chat or an existing one.
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ChatSetupControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.ChatCreateController
 * @see ai.tutor.cab302exceptionalhandlers.controller.ChatUpdateController
 */
public enum ChatSetupType {
    CREATE,
    UPDATE;
}
