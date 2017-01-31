package ru.noties.sqliteconnection.base;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import ru.noties.sqliteconnection.Statement;
import rx.Observable;
import rx.functions.Func0;

@SuppressWarnings("WeakerAccess")
public abstract class StatementBase<T> implements Statement<T> {

    private final SqlStatementBuilder mSqlStatementBuilder;

    protected StatementBase(String sql) {
        this.mSqlStatementBuilder = SqlStatementBuilder.create(sql);
    }

    @Override
    public Statement<T> bind(String name, boolean value) {
        mSqlStatementBuilder.bind(name, value ? 1 : 0);
        return this;
    }

    @Override
    public Statement<T> bind(String name, int value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<T> bind(String name, long value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<T> bind(String name, float value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<T> bind(String name, double value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<T> bind(String name, byte[] value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<T> bind(String name, String value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public void clearBindings() {
        mSqlStatementBuilder.clearBindings();
    }

    protected SqlStatementBuilder getSqlStatementBuilder() {
        return mSqlStatementBuilder;
    }

    @Override
    public Observable<T> toObservable() {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return Observable.just(execute());
            }
        });
    }
}
