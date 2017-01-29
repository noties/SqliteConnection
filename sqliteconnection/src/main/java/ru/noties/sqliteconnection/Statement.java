package ru.noties.sqliteconnection;

import rx.Observable;

public interface Statement<R> {

    Statement<R> bind(String name, boolean value);
    Statement<R> bind(String name, int value);
    Statement<R> bind(String name, long value);
    Statement<R> bind(String name, float value);
    Statement<R> bind(String name, double value);
    Statement<R> bind(String name, byte[] value);
    Statement<R> bind(String name, String value);
    void clearBindings();

    // idea: define interface, for example `Completable`
    // which has `toObservable` and `execute`

    Observable<R> toObservable();

    R execute();
}
