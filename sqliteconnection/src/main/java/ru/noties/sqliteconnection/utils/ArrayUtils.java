package ru.noties.sqliteconnection.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;

public class ArrayUtils {

    public static int length(@Nullable Object[] array) {
        return array != null
                ? array.length
                : 0;
    }

    public static <T> Iterable<T> toIterable(@NonNull T[] array) {
        return new ArrayIterable<>(array);
    }

    public static <T> String[] toStringArray(@Nullable T[] array) {

        final String[] out;

        final int length = length(array);
        if (length > 0) {
            out = new String[length];
            T object;
            for (int i = 0; i < length; i++) {
                //noinspection ConstantConditions
                object = array[i];
                if (object != null) {
                    out[i] = object.toString();
                }
            }
        } else {
            out = null;
        }

        return out;
    }

    private ArrayUtils() {}

    private static class ArrayIterable<T> implements Iterable<T> {

        private final T[] mArray;

        ArrayIterable(T[] array) {
            mArray = array;
        }

        @Override
        public Iterator<T> iterator() {
            return new ArrayIterator();
        }

        private class ArrayIterator implements Iterator<T> {

            int index = -1;

            @Override
            public boolean hasNext() {
                return (index + 1) < mArray.length;
            }

            @Override
            public T next() {
                return mArray[++index];
            }

            @Override
            public void remove() {
                throw new IllegalStateException("Not supported operation");
            }
        }
    }
}
