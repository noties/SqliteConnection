package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.sqliteconnection.system.ConnectionHandlerSystem;
import ru.noties.sqliteconnection.utils.Provider;

@SuppressWarnings("WeakerAccess")
public class SqliteDataSourceFactory {


    public static SqliteDataSource createSystem(
            @NonNull Provider<android.database.sqlite.SQLiteDatabase> provider
    ) {
        return createSystem(provider, null);
    }

    public static SqliteDataSource createSystem(
            @NonNull Provider<android.database.sqlite.SQLiteDatabase> provider,
            @Nullable ClosePolicy closePolicy
    ) {
        return new SqliteDataSource(provider, new ConnectionHandlerSystem(), closePolicy);
    }


    public static <DB> SqliteDataSource create(
            @NonNull Provider<DB> provider,
            @NonNull ConnectionHandler<DB> connectionHandler
    ) {
        return create(provider, connectionHandler, null);
    }

    public static <DB> SqliteDataSource create(
            @NonNull Provider<DB> provider,
            @NonNull ConnectionHandler<DB> connectionHandler,
            @Nullable ClosePolicy closePolicy
    ) {
        return new SqliteDataSource(provider, connectionHandler, closePolicy);
    }
}
