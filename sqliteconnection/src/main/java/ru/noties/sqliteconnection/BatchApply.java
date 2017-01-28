package ru.noties.sqliteconnection;

@SuppressWarnings("WeakerAccess")
public interface BatchApply<R, T> {
    R apply(Statement<R> statement, T value);
}
