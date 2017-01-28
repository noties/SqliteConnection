package ru.noties.sqliteconnection.system;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.SqliteConnectionBase;
import ru.noties.sqliteconnection.Statement;
import ru.noties.sqliteconnection.StatementBase;
import ru.noties.sqliteconnection.StatementBaseBatch;
import ru.noties.sqliteconnection.StatementInsert;
import ru.noties.sqliteconnection.StatementQuery;
import ru.noties.sqliteconnection.StatementUpdate;
import ru.noties.sqliteconnection.utils.ArrayUtils;

class SqliteConnectionSystem extends SqliteConnectionBase {

    // connection NEVER should close database

    private final SQLiteDatabase mDatabase;

    SqliteConnectionSystem(SQLiteDatabase database) {
        mDatabase = database;
    }

    @Override
    public void execute(@NonNull String sql, Object... args) {

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
        return new UpdateImpl(sql);
    }

    @Override
    public StatementInsert insert(@NonNull String sql) {
        return new InsertImpl(sql);
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

    // we be used by private classes
    private SqliteConnection asConnection() {
        return this;
    }

    private static <P extends SQLiteProgram> void bindAll(P program, Object[] args) {

        // TODO! NOW! We give ability to bind `byte[]`, so... it will be complete gibberish
        // if we call byte[].toString()...

        // todo, there is a way to bind `unsafe` by obtaining mBindArgs array from
        // SQLiteProgram and assign directly, but let's skip that. It's not 100%
        // that it will be faster than binding strings
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
                    // be pretty safe to assume that it can be only byte[]
                    if (o.getClass().isArray()) {
                        program.bindBlob(i + 1, (byte[]) o);
                    } else {
                        program.bindString(i + 1, o.toString());
                    }
                }
            }
        }
    }

    private class QueryImpl extends StatementBase<Cursor> implements StatementQuery {

        QueryImpl(String sql) {
            super(sql);
        }

        @Override
        public SqliteConnection getConnection() {
            return asConnection();
        }

        @Override
        public Cursor execute() {

            checkState();

            final SqlStatementBuilder builder = getSqlStatementBuilder();
            final String sql = builder.sqlStatement();
            final Object[] args = builder.sqlBindArguments();

            notifyOnExecution(sql, args);

            return mDatabase.rawQuery(sql, ArrayUtils.toStringArray(args));
        }

        @Override
        public Statement<Cursor> bind(String name, byte[] value) {
            // WE MUST throw as `rawQuery` accepts only String[], and converting
            // byte[] to string is just non-sense
            throw new IllegalStateException("Cannot bind `byte[]` (byte array) argument for query statement");
        }
    }

    private class UpdateImpl extends StatementBaseBatch<Integer> implements StatementUpdate {

        UpdateImpl(String sql) {
            super(sql);
        }

        @Override
        public SqliteConnection getConnection() {
            return asConnection();
        }

        @Override
        public Integer execute() {

            checkState();

            final int out;

            //noinspection unchecked
            final Batch<Integer, Object> batch = (Batch<Integer, Object>) popBatch();
            if (batch != null) {
                int sum = 0;
                for (Object o: batch.iterable()) {
                    sum += batch.batchApply().apply(this, o);
                }
                out = sum;
            } else {

                final SqlStatementBuilder builder = getSqlStatementBuilder();
                final String sql = builder.sqlStatement();
                final Object[] args = builder.sqlBindArguments();

                notifyOnExecution(sql, args);

                out = compileStatement(sql, args).executeUpdateDelete();
            }

            return out;
        }
    }

    private class InsertImpl extends StatementBaseBatch<Long> implements StatementInsert {

        InsertImpl(String sql) {
            super(sql);
        }

        @Override
        public SqliteConnection getConnection() {
            return asConnection();
        }

        @Override
        public Long execute() {

            checkState();

            final long out;

            //noinspection unchecked
            final Batch<Long, Object> batch = (Batch<Long, Object>) popBatch();
            if (batch != null) {
                long last = -1L;
                for (Object o: batch.iterable()) {
                    // so if we are here, and execute is called again -> store the result
                    last = batch.batchApply().apply(this, o);
                }
                out = last;
            } else {
                // simple execution
                final SqlStatementBuilder builder = getSqlStatementBuilder();
                final String sql = builder.sqlStatement();
                final Object[] args = builder.sqlBindArguments();
                notifyOnExecution(sql, args);
                out = compileStatement(sql, args).executeInsert();
            }

            return out;
        }
    }
}
