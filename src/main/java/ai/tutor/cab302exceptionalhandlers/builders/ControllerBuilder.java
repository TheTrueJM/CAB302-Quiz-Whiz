package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public abstract class ControllerBuilder<T> {
    protected final SQLiteConnection db;

    public ControllerBuilder(SQLiteConnection db) {
        this.db = db;
    }

    public abstract T build() throws Exception;
}
