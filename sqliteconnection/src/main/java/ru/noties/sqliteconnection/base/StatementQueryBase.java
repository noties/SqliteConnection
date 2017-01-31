package ru.noties.sqliteconnection.base;

import android.database.Cursor;

import ru.noties.sqliteconnection.StatementQuery;

@SuppressWarnings("WeakerAccess")
public abstract class StatementQueryBase extends StatementBase<Cursor> implements StatementQuery {

    protected StatementQueryBase(String sql) {
        super(sql);
    }

    @Override
    public StatementQuery bind(String name, boolean value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, int value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, long value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, float value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, double value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, byte[] value) {
        return (StatementQuery) super.bind(name, value);
    }

    @Override
    public StatementQuery bind(String name, String value) {
        return (StatementQuery) super.bind(name, value);
    }
}
