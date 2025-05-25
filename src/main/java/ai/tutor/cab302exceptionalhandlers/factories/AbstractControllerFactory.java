package ai.tutor.cab302exceptionalhandlers.factories;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

/**
 * Abstract base class for controller factories.
 * <p>
 * This class is the foundation for factories that create controller builders,
 * ensuring each factory is initialized with a {@link SQLiteConnection}.
 * <p>
 * Controller factories hold the same database connection address as it is a
 * singleton. This allows all controller builders produced by the factory
 * to share the same database connection.
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.factories.ControllerFactory
 */
public abstract class AbstractControllerFactory {
    protected final SQLiteConnection db;

    /**
     * Constructs an {@code AbstractControllerFactory}.
     *
     * @param db The {@link SQLiteConnection} to be used by the factory and subsequently
     *           by the controller builders it produces.
     */
    public AbstractControllerFactory(SQLiteConnection db) {
        this.db = db;
    }
}
