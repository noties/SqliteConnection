package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public class Transaction {

    public static Transaction begin(@NonNull SqliteConnection connection) {
        return new Transaction(connection)
                .beginTransaction();
    }

    private final SqliteConnection mConnection;
    private int mTransactionStack;

    public Transaction(@NonNull SqliteConnection mConnection) {
        this.mConnection = mConnection;
    }

    public SqliteConnection getConnection() {
        return mConnection;
    }

    // all transactions have additional semicolon before
    // to deal with DatabaseUtils bug (takes 3 first chars and tries to detect statement type,
    // so, in case of `rollback to $savepoint_name` it will execute just a rollback

    // Starts a deferred transaction
    public synchronized Transaction beginTransaction() {
        mConnection.execute(";BEGIN;");
        // only one transaction can be started (no nested ones)
        mTransactionStack = 1;
        return this;
    }

    public synchronized Transaction rollback() {
        mConnection.execute(";ROLLBACK;");
        mTransactionStack = 0;
        return this;
    }

    public synchronized Transaction commit() {
        // `commit` is used (instead of `end`) because cipher has a bug for sql statements less than 6 chars
        // https://github.com/sqlcipher/android-database-sqlcipher/issues/294
        mConnection.execute(";COMMIT;");
        mTransactionStack = 0;
        return this;
    }

    public synchronized Transaction savepoint(@NonNull String savepoint) {
        mConnection.execute(";SAVEPOINT '" + savepoint + "';");
        mTransactionStack += 1;
        return this;
    }

    public synchronized Transaction rollback(@NonNull String savepoint) {
        mConnection.execute(";ROLLBACK TO '" + savepoint + "';");
        mTransactionStack -= 1;
        return this;
    }

    public synchronized Transaction release(@NonNull String savepoint) {
        mConnection.execute(";RELEASE '" + savepoint + "';");
        mTransactionStack -= 1;
        return this;
    }

    public synchronized boolean isInTransaction() {
        return mTransactionStack > 0;
    }
}
