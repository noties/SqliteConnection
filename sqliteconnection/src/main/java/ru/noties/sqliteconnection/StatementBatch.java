package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

public interface StatementBatch<R> extends Statement<R> {

    <T> StatementBatch<R> batch(@NonNull Iterable<T> iterable, @NonNull BatchApply<R, T> batchApply);
    <T> StatementBatch<R> batch(@NonNull T[] array, @NonNull BatchApply<R, T> batchApply);

    // if not cleared the batch will be kept
    void clearBatch();


    // these are overridden to give ability to combine `batch` and `bind` methods
    @Override
    StatementBatch<R> bind(String name, boolean value);

    @Override
    StatementBatch<R> bind(String name, int value);

    @Override
    StatementBatch<R> bind(String name, long value);

    @Override
    StatementBatch<R> bind(String name, float value);

    @Override
    StatementBatch<R> bind(String name, double value);

    @Override
    StatementBatch<R> bind(String name, byte[] value);

    @Override
    StatementBatch<R> bind(String name, String value);
}
