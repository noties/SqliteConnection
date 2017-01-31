package ru.noties.sqliteconnection;

import android.database.Cursor;
import android.support.annotation.NonNull;

public interface StatementQuery extends Statement<Cursor> {

    interface RowMapper<T> {
        T map(Cursor cursor);
    }

    <T> StatementQueryMap<T> map(@NonNull RowMapper<T> mapper);


    @Override
    StatementQuery bind(String name, boolean value);

    @Override
    StatementQuery bind(String name, int value);

    @Override
    StatementQuery bind(String name, long value);

    @Override
    StatementQuery bind(String name, float value);

    @Override
    StatementQuery bind(String name, double value);

    @Override
    StatementQuery bind(String name, byte[] value);

    @Override
    StatementQuery bind(String name, String value);
}
