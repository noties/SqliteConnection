package ru.noties.sqliteconnection;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import rx.Observable;
import rx.functions.Func0;

public abstract class StatementBase<R> implements Statement<R> {

    private final SqlStatementBuilder mSqlStatementBuilder;

    protected StatementBase(String sql) {
        this.mSqlStatementBuilder = SqlStatementBuilder.create(sql);
    }

    @Override
    public Statement<R> bind(String name, boolean value) {
        mSqlStatementBuilder.bind(name, value ? 1 : 0);
        return this;
    }

    @Override
    public Statement<R> bind(String name, int value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<R> bind(String name, long value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<R> bind(String name, float value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<R> bind(String name, double value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<R> bind(String name, byte[] value) {
        mSqlStatementBuilder.bind(name, value);
        return this;
    }

    @Override
    public Statement<R> bind(String name, String value) {
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
    public Observable<R> toObservable() {
        return Observable.defer(new Func0<Observable<R>>() {
            @Override
            public Observable<R> call() {
                return Observable.just(execute());
            }
        });
    }
}
