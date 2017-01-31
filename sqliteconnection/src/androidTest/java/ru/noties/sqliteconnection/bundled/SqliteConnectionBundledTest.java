package ru.noties.sqliteconnection.bundled;

import org.sqlite.database.sqlite.SQLiteDatabase;

import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.SqliteConnectionTestBase;
import ru.noties.sqliteconnection.SqliteDataSource;
import ru.noties.sqliteconnection.SqliteDataSourceFactory;
import ru.noties.sqliteconnection.utils.Provider;

public class SqliteConnectionBundledTest extends SqliteConnectionTestBase {

    static {
        System.loadLibrary("sqliteX");
    }

    @Override
    protected SqliteConnection getConnection() {
        final SqliteDataSource dataSource = SqliteDataSourceFactory.createBundled(new Provider<SQLiteDatabase>() {
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
        return !((SqliteConnectionBundled) connection).getDatabase().isOpen();
    }
}
