package ru.noties.sqliteconnection.system;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import ru.noties.sqliteconnection.SqliteConnectionBase;
import ru.noties.sqliteconnection.StatementInsert;
import ru.noties.sqliteconnection.StatementQuery;
import ru.noties.sqliteconnection.StatementUpdate;
import ru.noties.sqliteconnection.utils.ArrayUtils;

class SqliteConnectionSystem extends SqliteConnectionBase {

    // connection should NEVER close database

    private final SQLiteDatabase mDatabase;

    SqliteConnectionSystem(SQLiteDatabase database) {
        mDatabase = database;
    }

    // All passed objects will be converted to String (via `toString()` method),
    // except for byte[]. If any array except byte[] will be passed here (as one of the arguments of cause)
    // runtime exception with illegal cast will be thrown (we do not validate the data and expect
    // in case of array it always to be byte[])
    //
    // Also, it may be a good idea to convert floating numbers to string manually (to avoid confusion and
    // unexpected results)
    @Override
    public void execute(@NonNull String sql, Object... args) {

        // we have introduced `close` logic, so it's better to fail-fast if
        // connection is already closed (to detect bugs early) even if
        // underlying database is still opened
        checkState();

        notifyOnExecution(sql, args);

        compileStatement(sql, args).execute();
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public StatementQuery query(@NonNull String sql) {
        return new QueryImpl(sql);
    }

    @Override
    public StatementUpdate update(@NonNull String sql) {
        return new UpdateImpl(sql, new UpdateFunc());
    }

    @Override
    public StatementInsert insert(@NonNull String sql) {
        return new InsertImpl(sql, new InsertFunc());
    }

    @Override
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    @Override
    public void beginTransactionNonExclusive() {
        mDatabase.beginTransactionNonExclusive();
    }

    @Override
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        mDatabase.endTransaction();
    }

    @Override
    public boolean yieldIfContendedSafely() {
        return mDatabase.yieldIfContendedSafely();
    }

    @Override
    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return mDatabase.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    @Override
    public boolean inTransaction() {
        return mDatabase.inTransaction();
    }

    private SQLiteStatement compileStatement(String sql, Object[] args) {
        final SQLiteStatement statement = mDatabase.compileStatement(sql);
        bindAll(statement, args);
        return statement;
    }

    @Override
    protected String databaseToString() {
        return mDatabase.toString();
    }


    private static <P extends SQLiteProgram> void bindAll(P program, Object[] args) {

        // there is a hacky way to obtain Object[] mBindArgs from SQLiteProgram
        // and modify it directly, but it's not 100% that it will be faster than binding strings.
        // Although we do not accept `bad` types (the types that are not supported by SQLiteDatabase
        // with the help of overloaded methods that accept only correct types, putting all the
        // chips on the idea tha private API/fields will be present on all devices and configurations
        // is a bit odd. And binding arguments as strings is relatively safe (except maybe floating
        // numbers, we need to specify it in README that it's better to convert floats to strings
        // manually)

        final int length = ArrayUtils.length(args);
        if (length > 0) {
            Object o;
            for (int i = 0; i < length; i++) {
                o = args[i];
                if (o == null) {
                    program.bindNull(i + 1);
                } else {
                    // it's really important to bind byte[] independently
                    // we only support one type of array -> byte[], so it must
                    // be pretty safe to assume that it can be only byte[].
                    // if by any chance there will be something other than byte[] here,
                    // we will fail with cast exception (so, no need to validate it here)
                    if (o.getClass().isArray()) {
                        program.bindBlob(i + 1, (byte[]) o);
                    } else {
                        program.bindString(i + 1, o.toString());
                    }
                }
            }
        }
    }

    private class QueryImpl extends QueryBase {

        QueryImpl(String sql) {
            super(sql);
        }

        @Override
        protected Cursor executeInner(String sql, Object[] args) {
            return mDatabase.rawQuery(sql, ArrayUtils.toStringArray(args));
        }
    }

    private class UpdateFunc extends BatchFuncUpdateBase {

        @Override
        protected Integer executeInner(String sql, Object[] args) {
            return compileStatement(sql, args).executeUpdateDelete();
        }
    }

    private class InsertFunc extends BatchFuncInsertBase {

        @Override
        protected Long executeInner(String sql, Object[] args) {
            return compileStatement(sql, args).executeInsert();
        }
    }
}
