package ru.noties.sqliteconnection;

public class ValueMutable<T> {

    private T mValue;

    public ValueMutable() {

    }

    public ValueMutable(T initial) {
        mValue = initial;
    }

    public T get() {
        return mValue;
    }

    public void set(T value) {
        mValue = value;
    }
}
