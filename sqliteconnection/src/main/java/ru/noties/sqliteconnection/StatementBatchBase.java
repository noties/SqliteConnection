package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.noties.sqlbuilder.SqlStatementBuilder;
import ru.noties.sqliteconnection.utils.ArrayUtils;

public abstract class StatementBatchBase<R> extends StatementBase<R> implements StatementBatch<R> {

    public interface Func<R> {
        R combine(@Nullable R left, R right);
        R execute(String sql, Object[] args);
    }

    private final Func<R> mFunc;
    private Batch<R, ?> mBatch;

    protected StatementBatchBase(String sql, Func<R> func) {
        super(sql);
        mFunc = func;
    }

    @Override
    public <T> StatementBatch<R> batch(@NonNull Iterable<T> iterable, @NonNull BatchApply<R, T> batchApply) {
        mBatch = new Batch<>(iterable, batchApply);
        return this;
    }

    @Override
    public <T> StatementBatch<R> batch(@NonNull T[] array, @NonNull BatchApply<R, T> batchApply) {
        mBatch = new Batch<>(ArrayUtils.toIterable(array), batchApply);
        return this;
    }

    @Override
    public void clearBatch() {
        // does not take into account if we are currently iterating over batch...
        mBatch = null;
    }

    @Override
    public R execute() {

        final R out;

        // so, we will remove locally stored batch instance if present
        // in order to execute it `normally` and after iteration is done
        // we will set it back
        // batch is cleared by another batch or when `clearBindings` is called

        //noinspection unchecked
        final Batch<R, Object> batch = (Batch<R, Object>) mBatch;
        mBatch = null;

        if (batch != null) {
            R inner = null;
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
    public StatementBatch<R> bind(String name, boolean value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, int value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, long value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, float value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, double value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, byte[] value) {
        return (StatementBatch<R>) super.bind(name, value);
    }

    @Override
    public StatementBatch<R> bind(String name, String value) {
        return (StatementBatch<R>) super.bind(name, value);
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
