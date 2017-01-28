package ru.noties.sqliteconnection.system;

import android.database.sqlite.SQLiteDatabase;

import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.SqliteConnectionTestBase;
import ru.noties.sqliteconnection.SqliteDataSource;
import ru.noties.sqliteconnection.SqliteDataSourceFactory;
import ru.noties.sqliteconnection.utils.Provider;

public class SqliteConnectionSystemTest extends SqliteConnectionTestBase {

    @Override
    protected SqliteConnection getConnection() {
        final SqliteDataSource dataSource = SqliteDataSourceFactory.createSystem(new Provider<SQLiteDatabase>() {
            @Override
            public SQLiteDatabase provide() {
                // memory
                return SQLiteDatabase.create(null);
            }
        });
        return dataSource.open();
    }

    @Override
    protected boolean isDatabaseClosed(SqliteConnection connection) {
        return !((SqliteConnectionSystem) connection).getDatabase().isOpen();
    }
}
