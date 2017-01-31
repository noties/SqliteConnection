package ru.noties.sqliteconnection;

import rx.Observable;

public interface Statement<T> {

    Statement<T> bind(String name, boolean value);
    Statement<T> bind(String name, int value);
    Statement<T> bind(String name, long value);
    Statement<T> bind(String name, float value);
    Statement<T> bind(String name, double value);
    Statement<T> bind(String name, byte[] value);
    Statement<T> bind(String name, String value);
    void clearBindings();

    Observable<T> toObservable();

    T execute();
}
