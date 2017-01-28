package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

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
}
