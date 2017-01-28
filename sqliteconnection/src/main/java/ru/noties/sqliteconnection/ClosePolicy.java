package ru.noties.sqliteconnection;

public interface ClosePolicy {
    void onNewConnectionRequested(SqliteDataSource controller);
    boolean onAdditionalConnectionOpen(SqliteDataSource controller);
    boolean onLastConnectionClose(SqliteDataSource connectionController);
}
