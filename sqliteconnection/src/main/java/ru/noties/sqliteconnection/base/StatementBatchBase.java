package ru.noties.sqliteconnection.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import ru.noties.sqliteconnection.BatchApply;
import ru.noties.sqliteconnection.StatementBatch;
import ru.noties.sqliteconnection.utils.ArrayUtils;

@SuppressWarnings("WeakerAccess")
public abstract class StatementBatchBase<T> extends StatementBase<T> implements StatementBatch<T> {

    public interface Func<T> {
        T combine(@Nullable T left, T right);
        T execute(String sql, Object[] args);
    }

    private final Func<T> mFunc;
    private Batch<T, ?> mBatch;

    protected StatementBatchBase(String sql, Func<T> func) {
        super(sql);
        mFunc = func;
    }

    @Override
    public <R> StatementBatch<T> batch(@NonNull Iterable<R> iterable, @NonNull BatchApply<T, R> batchApply) {
        mBatch = new Batch<>(iterable, batchApply);
        return this;
    }

    @Override
    public <R> StatementBatch<T> batch(@NonNull R[] array, @NonNull BatchApply<T, R> batchApply) {
        mBatch = new Batch<>(ArrayUtils.toIterable(array), batchApply);
        return this;
    }

    @Override
    public void clearBatch() {
        // does not take into account if we are currently iterating over batch...
        mBatch = null;
    }

    @Override
    public T execute() {

        final T out;

        // so, we will remove locally stored batch instance if present
        // in order to execute it `normally` and after iteration is done
        // we will set it back
        // batch is cleared by another batch or when `clearBindings` is called

        //noinspection unchecked
        final Batch<T, Object> batch = (Batch<T, Object>) mBatch;
        mBatch = null;

        if (batch != null) {
            T inner = null;
            for (Object o: batch.mIterable) {
                inner = mFunc.combine(inner, batch.mBatchApply.apply(this, o));
            }
            out = inner;
            mBatch = batch;
        } else {
            final SqlStatementBuilder builder = getSqlStatementBuilder();
            out = mFunc.execute(builder.sqlStatement(), builder.sqlBindArguments());
        }

        return out;
    }

    @Override
    public StatementBatch<T> bind(String name, boolean value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, int value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, long value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, float value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, double value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, byte[] value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    @Override
    public StatementBatch<T> bind(String name, String value) {
        return (StatementBatch<T>) super.bind(name, value);
    }

    // todo, what can we do here:
    // we can check if we have pending batch and throw an exception
    // if `bind` methods were called
    // most likely it's an error when both `ways` are combined
    // also, it doesn't make sense to have multiple batches...

    private static class Batch<R, T> {

        final Iterable<T> mIterable;
        final BatchApply<R, T> mBatchApply;

        Batch(Iterable<T> iterable, BatchApply<R, T> batchApply) {
            mIterable = iterable;
            mBatchApply = batchApply;
        }
    }
}
