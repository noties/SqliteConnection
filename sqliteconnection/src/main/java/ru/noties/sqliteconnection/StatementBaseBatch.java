package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

import ru.noties.sqliteconnection.utils.ArrayUtils;

public abstract class StatementBaseBatch<R> extends StatementBase<R> implements StatementBatch<R> {

    private Batch<R, ?> mBatch;

    protected StatementBaseBatch(String sql) {
        super(sql);
    }

    protected Batch<R, ?> popBatch() {
        final Batch<R, ?> temp = mBatch;
        mBatch = null;
        return temp;
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


    protected static class Batch<R, T> {

        private final Iterable<T> mIterable;
        private final BatchApply<R, T> mBatchApply;

        Batch(Iterable<T> iterable, BatchApply<R, T> batchApply) {
            mIterable = iterable;
            mBatchApply = batchApply;
        }

        public Iterable<T> iterable() {
            return mIterable;
        }

        public BatchApply<R, T> batchApply() {
            return mBatchApply;
        }
    }
}
