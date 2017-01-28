package ru.noties.sqliteconnection.system;

import android.database.sqlite.SQLiteDatabase;

import ru.noties.sqliteconnection.ConnectionHandler;
import ru.noties.sqliteconnection.SqliteConnection;

public class ConnectionHandlerSystem implements ConnectionHandler<SQLiteDatabase> {

    @Override
    public SqliteConnection open(SQLiteDatabase database) {
        return new SqliteConnectionSystem(database);
    }

    @Override
    public void close(SQLiteDatabase database) {
        database.close();
    }
}
