package ru.noties.sqliteconnection.base;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.HashSet;
import java.util.Set;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.StatementInsert;
import ru.noties.sqliteconnection.StatementQuery;
import ru.noties.sqliteconnection.StatementQueryMap;
import ru.noties.sqliteconnection.StatementUpdate;

public abstract class SqliteConnectionBase implements SqliteConnection {

    // StateObserver accepts SqliteConnection as an argument
    // meaning StateObserver can unregister itself, so, we need to
    private Set<StateObserver> mStateObservers;
    private boolean mIsClosed;

    @Override
    public synchronized void close() {
        // we should close and notify only once
        if (!mIsClosed) {
            mIsClosed = true;
            notifyOnClosed();
        }
    }

    @Override
    public synchronized boolean isClosed() {
        return mIsClosed;
    }

    protected synchronized void checkState() {
        if (mIsClosed) {
            throw new IllegalStateException("SqliteConnection is already closed: `" + toString() + "`");
        }
    }

    @Override
    public void registerStateObserver(@NonNull StateObserver observer) {
        if (mStateObservers == null) {
            mStateObservers = new HashSet<>(3);
        }
        mStateObservers.add(observer);
    }

    @Override
    public void unregisterStateObserver(@NonNull StateObserver observer) {
        if (mStateObservers != null) {
            mStateObservers.remove(observer);
        }
    }

    protected void notifyOnClosed() {
        if (mStateObservers != null && mStateObservers.size() > 0) {
            // in order to give ability to remove self (StateObserver) whilst iterating over collection
            for (StateObserver observer: new HashSet<>(mStateObservers)) {
                observer.onClosed(this);
            }
        }
    }

    protected void notifyOnExecution(String sql, Object[] args) {
        if (mStateObservers != null && mStateObservers.size() > 0) {
            // in order to give ability to remove self (StateObserver) whilst iterating over collection
            for (StateObserver observer: new HashSet<>(mStateObservers)) {
                observer.onExecute(this, sql, args);
            }
        }
    }

    protected abstract String databaseToString();

    @Override
    public final String toString() {
        return getClass().getSimpleName() +
                "(isClosed: " + mIsClosed +
                ", database: `" + databaseToString() +
                "`)";
    }

    @VisibleForTesting
    public int getStateObserversSize() {
        return mStateObservers != null ? mStateObservers.size() : 0;
    }

    protected abstract class QueryBase extends StatementQueryBase {

        protected QueryBase(String sql) {
            super(sql);
        }

        @Override
        public Cursor execute() {

            checkState();

            final SqlStatementBuilder builder = getSqlStatementBuilder();
            final String sql = builder.sqlStatement();
            final Object[] args = builder.sqlBindArguments();

            notifyOnExecution(sql, args);

            return executeInner(sql, args);
        }

        @Override
        public <T> StatementQueryMap<T> map(@NonNull RowMapper<T> mapper) {
            return new StatementQueryMapImpl<>(this, mapper);
        }

        @Override
        public StatementQuery bind(String name, byte[] value) {
            // WE MUST throw as `rawQuery` accepts only String[], and converting
            // byte[] to string is just a non-sense
            throw new IllegalStateException("Cannot bind `byte[]` (byte array) argument for query statement");
        }

        protected abstract Cursor executeInner(String sql, Object[] args);
    }

    private abstract class BatchFuncBase<T> implements StatementBatchBase.Func<T> {
        @Override
        public T execute(String sql, Object[] args) {
            checkState();
            notifyOnExecution(sql, args);
            return executeInner(sql, args);
        }

        protected abstract T executeInner(String sql, Object[] args);
    }

    protected abstract class BatchFuncUpdateBase extends BatchFuncBase<Integer> {

        @Override
        public Integer combine(@Nullable Integer left, Integer right) {
            return left == null
                    ? right
                    : left + right;
        }
    }

    protected abstract class BatchFuncInsertBase extends BatchFuncBase<Long> {
        @Override
        public Long combine(@Nullable Long left, Long right) {
            return right;
        }
    }

    protected static class UpdateImpl extends StatementBatchBase<Integer> implements StatementUpdate {

        public UpdateImpl(String sql, Func<Integer> func) {
            super(sql, func);
        }
    }

    protected static class InsertImpl extends StatementBatchBase<Long> implements StatementInsert {

        public InsertImpl(String sql, Func<Long> func) {
            super(sql, func);
        }
    }
}
