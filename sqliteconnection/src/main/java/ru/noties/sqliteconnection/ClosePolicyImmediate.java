package ru.noties.sqliteconnection;

public class ClosePolicyImmediate implements ClosePolicy {

    // closes immediately after last connection is closed

    @Override
    public void onNewConnectionRequested(SqliteDataSource controller) {
        // no op
    }

    @Override
    public boolean onAdditionalConnectionOpen(SqliteDataSource controller) {
        return true;
    }

    @Override
    public boolean onLastConnectionClose(SqliteDataSource connectionController) {
        return true;
    }
}
