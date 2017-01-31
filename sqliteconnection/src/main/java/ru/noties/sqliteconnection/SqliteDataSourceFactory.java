package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.sqliteconnection.bundled.ConnectionHandlerBundled;
import ru.noties.sqliteconnection.cipher.ConnectionHandlerCipher;
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

    public static SqliteDataSource createBundled(
            @NonNull Provider<org.sqlite.database.sqlite.SQLiteDatabase> provider
    ) {
        return createBundled(provider, null);
    }

    public static SqliteDataSource createBundled(
            @NonNull Provider<org.sqlite.database.sqlite.SQLiteDatabase> provider,
            @Nullable ClosePolicy closePolicy
    ) {
        return new SqliteDataSource(provider, new ConnectionHandlerBundled(), closePolicy);
    }

    public static SqliteDataSource createCipher(
            @NonNull Provider<net.sqlcipher.database.SQLiteDatabase> provider
    ) {
        return createCipher(provider, null);
    }

    public static SqliteDataSource createCipher(
            @NonNull Provider<net.sqlcipher.database.SQLiteDatabase> provider,
            @Nullable ClosePolicy closePolicy
    ) {
        return new SqliteDataSource(provider, new ConnectionHandlerCipher(), closePolicy);
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
