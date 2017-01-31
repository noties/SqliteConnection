package ru.noties.sqliteconnection;

import java.util.List;

public interface StatementQueryMap<T> extends Statement<T> {

    StatementQueryMap<List<T>> asList();

    @Override
    StatementQueryMap<T> bind(String name, boolean value);

    @Override
    StatementQueryMap<T> bind(String name, int value);

    @Override
    StatementQueryMap<T> bind(String name, long value);

    @Override
    StatementQueryMap<T> bind(String name, float value);

    @Override
    StatementQueryMap<T> bind(String name, double value);

    @Override
    StatementQueryMap<T> bind(String name, byte[] value);

    @Override
    StatementQueryMap<T> bind(String name, String value);
}
