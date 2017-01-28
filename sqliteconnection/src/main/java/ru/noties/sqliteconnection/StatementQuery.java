package ru.noties.sqliteconnection;

import android.database.Cursor;

public interface StatementQuery extends Statement<Cursor> {
    // todo,
    // stream (String tableName) -> subscribes for table changes notification
    // need a way to do it possible only if toObservable is called
}
