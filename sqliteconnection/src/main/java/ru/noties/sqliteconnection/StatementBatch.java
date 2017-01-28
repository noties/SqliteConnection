package ru.noties.sqliteconnection;

import android.support.annotation.NonNull;

interface StatementBatch<R> extends Statement<R> {
    <T> StatementBatch<R> batch(@NonNull Iterable<T> iterable, @NonNull BatchApply<R, T> batchApply);
    <T> StatementBatch<R> batch(@NonNull T[] array, @NonNull BatchApply<R, T> batchApply);
}
