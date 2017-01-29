package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

import java.io.Closeable;

public interface SqliteConnection extends Closeable {

    abstract class StateObserver {
        public void onExecute(SqliteConnection connection, String sql, Object[] bindArgs) {}
        public void onClosed(SqliteConnection connection) {}
    }

    void execute(@NonNull String sql, Object... args);

    StatementQuery query(@NonNull String sql);

    StatementUpdate update(@NonNull String sql);
    StatementInsert insert(@NonNull String sql);


    void beginTransaction();
    void beginTransactionNonExclusive();
    void setTransactionSuccessful();
    void endTransaction();

    boolean yieldIfContendedSafely();
    boolean yieldIfContendedSafely(long sleepAfterYieldDelay);

    boolean inTransaction();


    @Override
    void close();
    boolean isClosed();

    void registerStateObserver  (@NonNull StateObserver observer);
    void unregisterStateObserver(@NonNull StateObserver observer);
}
