package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import ru.noties.sqliteconnection.utils.Provider;

@SuppressWarnings("WeakerAccess")
public class SqliteDataSource {

    private final Provider mDatabaseProvider;
    private final ConnectionHandler mConnectionHandler;
    private final ClosePolicy mClosePolicy;

    private final Object mLock = new Object();

    private final SqliteConnection.StateObserver mCloseObserver = new SqliteConnection.StateObserver() {
        @Override
        public void onClosed(SqliteConnection connection) {
            SqliteDataSource.this.close();
            connection.unregisterStateObserver(this);
        }
    };

    private int mOpenCount;
    private Object mDatabase;
    private SqliteConnection.StateObserver mAdditionalStateObserver;

    SqliteDataSource(
            @NonNull Provider provider,
            @NonNull ConnectionHandler connectionHandler,
            @Nullable ClosePolicy closePolicy
    ) {
        mDatabaseProvider = provider;
        mConnectionHandler = connectionHandler;
        mClosePolicy = closePolicy == null ? new ClosePolicyImmediate() : closePolicy;
    }

    // registers `default` stateObserver for each connection created by this dataSource (useful for logging)
    // please note, that this observer MUST unregister in `onClosed` method
    // otherwise connection will be still referenced and kept from gc'ing
    public SqliteDataSource registerDefaultStateObserver(@NonNull SqliteConnection.StateObserver observer) {
        synchronized (mLock) {
            mAdditionalStateObserver = observer;
            return this;
        }
    }

    public SqliteConnection open() {
        synchronized (mLock) {
            mClosePolicy.onNewConnectionRequested(this);
            if (mOpenCount == 0) {
                mDatabase = mDatabaseProvider.provide(); // obtain
                mOpenCount = 1;
            } else if (mClosePolicy.onAdditionalConnectionOpen(this)) {
                mOpenCount += 1;
            }
            //noinspection unchecked
            final SqliteConnection connection = mConnectionHandler.open(mDatabase);
            connection.registerStateObserver(mCloseObserver);
            if (mAdditionalStateObserver != null) {
                connection.registerStateObserver(mAdditionalStateObserver);
            }
            return connection;
        }
    }

    // PLEASE note that this method is not intended to be used by anyone except ClosePolicy
    // if a ClosePolicy needs to close this DataSource - use ClosePolicyHelper.close
    void close() {
        synchronized (mLock) {
            if (mOpenCount == 1) {
                if (mClosePolicy.onLastConnectionClose(this)) {
                    //noinspection unchecked
                    mConnectionHandler.close(mDatabase);
                    mOpenCount = 0;
                }
            } else {
                mOpenCount -= 1;
            }
        }
    }
}
