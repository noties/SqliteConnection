package ru.noties.sqliteconnection;

public interface ConnectionHandler<T> {
    SqliteConnection open(T database);
    void close(T database);
}
