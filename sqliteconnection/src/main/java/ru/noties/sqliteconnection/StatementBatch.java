package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

public interface StatementBatch<T> extends Statement<T> {

    <R> StatementBatch<T> batch(@NonNull Iterable<R> iterable, @NonNull BatchApply<T, R> batchApply);
    <R> StatementBatch<T> batch(@NonNull R[] array, @NonNull BatchApply<T, R> batchApply);

    // if not cleared the batch will be kept
    // but it doesn't check if we are currently iterating over it
    // So, we need to specify
    void clearBatch();


    // these are overridden to give ability to combine `batch` and `bind` methods
    @Override
    StatementBatch<T> bind(String name, boolean value);

    @Override
    StatementBatch<T> bind(String name, int value);

    @Override
    StatementBatch<T> bind(String name, long value);

    @Override
    StatementBatch<T> bind(String name, float value);

    @Override
    StatementBatch<T> bind(String name, double value);

    @Override
    StatementBatch<T> bind(String name, byte[] value);

    @Override
    StatementBatch<T> bind(String name, String value);
}
