package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

public class ClosePolicyHelper {

    // to be used directly by ClosePolicy. We need to extract this in order
    // not to create confusion about method `close` in SqliteDataSource, which
    // is intended to be used only by closePolicy
    public void closeDataSource(@NonNull SqliteDataSource dataSource) {
        dataSource.close();
    }

    private ClosePolicyHelper() {}
}
