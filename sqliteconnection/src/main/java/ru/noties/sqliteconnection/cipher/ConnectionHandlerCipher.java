package ru.noties.sqliteconnection.cipher;

import net.sqlcipher.database.SQLiteDatabase;

import ru.noties.sqliteconnection.ConnectionHandler;
import ru.noties.sqliteconnection.SqliteConnection;

public class ConnectionHandlerCipher implements ConnectionHandler<SQLiteDatabase> {

    @Override
    public SqliteConnection open(SQLiteDatabase database) {
        return new SqliteConnectionCipher(database);
    }

    @Override
    public void close(SQLiteDatabase database) {
        database.close();
    }
}
