package ru.noties.sqliteconnection.bundled;

import org.sqlite.database.sqlite.SQLiteDatabase;

import ru.noties.sqliteconnection.ConnectionHandler;
import ru.noties.sqliteconnection.SqliteConnection;

public class ConnectionHandlerBundled implements ConnectionHandler<SQLiteDatabase> {

    @Override
    public SqliteConnection open(SQLiteDatabase database) {
        return new SqliteConnectionBundled(database);
    }

    @Override
    public void close(SQLiteDatabase database) {
        database.close();
    }
}
