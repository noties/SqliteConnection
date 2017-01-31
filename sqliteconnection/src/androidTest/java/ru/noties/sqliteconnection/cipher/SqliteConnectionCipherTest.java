package ru.noties.sqliteconnection.cipher;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.SqliteConnectionTestBase;
import ru.noties.sqliteconnection.SqliteDataSource;
import ru.noties.sqliteconnection.SqliteDataSourceFactory;
import ru.noties.sqliteconnection.utils.Provider;

public class SqliteConnectionCipherTest extends SqliteConnectionTestBase {

    static {
        SQLiteDatabase.loadLibs(InstrumentationRegistry.getTargetContext());
    }

    @Override
    protected SqliteConnection getConnection() {

        // we would use memory database here (like system & bundled), but
        // cipher doesn't support it (of cause there is no much sense in using
        // encrypted memory database)

        final Context context = InstrumentationRegistry.getTargetContext();
        final File path = context.getDatabasePath("cipher.sqlite3");
        if (path.exists()) {
            if (!path.delete()) {
                throw new RuntimeException("Cannot delete database file: " + path.getAbsolutePath());
            }
        } else {
            final File parent = path.getParentFile();
            if (parent == null) {
                throw new RuntimeException("Cannot access parent folder for a database file at path: " + path.getAbsolutePath());
            } else if (!parent.exists() && !parent.mkdirs()) {
                throw new RuntimeException("Cannot `mkdirs` for a folder: " + parent.getAbsolutePath());
            }
        }

        final SqliteDataSource dataSource = SqliteDataSourceFactory.createCipher(new Provider<SQLiteDatabase>() {
            @Override
            public SQLiteDatabase provide() {
                return SQLiteDatabase.openOrCreateDatabase(path, "cipher 12300", null);
            }
        });
        return dataSource.open();
    }

    @Override
    protected boolean isDatabaseClosed(SqliteConnection connection) {
        return !((SqliteConnectionCipher) connection).getDatabase().isOpen();
    }
}
