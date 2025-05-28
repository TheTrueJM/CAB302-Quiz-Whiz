package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

/**
 * Abstract base class for all controller builders.
 * <p>
 * This class provides a common structure for controller factories to dynamically
 * create their respective controllers, ensuring each builder has access to a
 * {@link SQLiteConnection}.
 *
 * @author Justin.
 * @param <T> The type of controller to be built.
 */
public abstract class ControllerBuilder<T> {
    protected final SQLiteConnection db;

    /**
     * Constructs a {@code ControllerBuilder} with the given database connection.
     *
     * @param db The {@link SQLiteConnection} to be used by the builder and the resulting controller.
     */
    public ControllerBuilder(SQLiteConnection db) {
        this.db = db;
    }

    /**
     * Abstract method to build the controller.
     * <p>
     * Subclasses must implement this method to return an instance of the controller
     * of type {@code <T>}, configured with parameters set via the builder's methods.
     *
     * @return The built controller.
     * @throws Exception if an error occurs during the controller construction process.
     */
    public abstract T build() throws Exception;
}
