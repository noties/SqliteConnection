package ru.noties.sqliteconnection.base;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.noties.sqliteconnection.StatementQuery;
import ru.noties.sqliteconnection.StatementQueryMap;

class StatementQueryMapImpl<T> extends StatementBase<T> implements StatementQueryMap<T> {

    private final StatementQuery mQuery;
    private final StatementQuery.RowMapper<T> mRowMapper;
    private boolean mIsList;

    StatementQueryMapImpl(StatementQuery query, StatementQuery.RowMapper<T> rowMapper) {
        super("");
        mQuery = query;
        mRowMapper = rowMapper;
    }

    @Override
    public StatementQueryMap<T> bind(String name, boolean value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, int value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, long value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, float value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, double value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, byte[] value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<T> bind(String name, String value) {
        mQuery.bind(name, value);
        return this;
    }

    @Override
    public StatementQueryMap<List<T>> asList() {
        mIsList = true;
        //noinspection unchecked
        return (StatementQueryMap<List<T>>) this;
    }

    @Override
    public T execute() {

        final T out;

        final Cursor cursor = mQuery.execute();
        if (cursor != null) {

            try {

                if (mIsList) {
                    final List list;
                    final int count = cursor.getCount();
                    if (count == 0) {
                        list = Collections.EMPTY_LIST;
                    } else {
                        list = new ArrayList(count);
                        while (cursor.moveToNext()) {
                            //noinspection unchecked
                            list.add(mRowMapper.map(cursor));
                        }
                    }
                    //noinspection unchecked
                    out = (T) list;
                } else {
                    if (cursor.moveToFirst()) {
                        out = mRowMapper.map(cursor);
                    } else {
                        out = null;
                    }
                }

            } finally {
                cursor.close();
            }
        } else {
            out = null;
        }

        return out;
    }
}
