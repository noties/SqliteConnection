package ru.noties.sqliteconnection;

public class ClosePolicyKeepAlive implements ClosePolicy {

    // after connection is opened it won't be closed (aka keep-alive)

    @Override
    public void onNewConnectionRequested(SqliteDataSource controller) {
        // no op
    }

    @Override
    public boolean onAdditionalConnectionOpen(SqliteDataSource controller) {
        return false;
    }

    @Override
    public boolean onLastConnectionClose(SqliteDataSource connectionController) {
        return false;
    }
}
