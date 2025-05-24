package ai.tutor.cab302exceptionalhandlers.factories;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public abstract class AbstractControllerFactory {
    protected final SQLiteConnection db;

    public AbstractControllerFactory(SQLiteConnection db) {
        this.db = db;
    }
}
