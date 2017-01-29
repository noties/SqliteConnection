package ru.noties.sqliteconnetion.sample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.noties.debug.Debug;
import ru.noties.debug.out.AndroidLogDebugOutput;

public class MainActivity extends AppCompatActivity {

    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    public interface Callback {
        void apply();
    }

    private static class Run implements Runnable {

        private final SQLiteDatabase db;
        private final String name;
        private final boolean commit;
        private final Callback callback;

        private Run(SQLiteDatabase db, String name, boolean commit, Callback callback) {
            this.db = db;
            this.name = name;
            this.commit = commit;
            this.callback = callback;
        }

        @Override
        public void run() {

            Thread.currentThread().setName(name);
            Debug.i("name: %s", name);

            try {
//                db.execSQL(";begin deferred transaction");
                db.beginTransactionNonExclusive();
                for (int i = 0; i < 1000; i++) {
                    db.execSQL("insert into t(name) values(?);", new Object[] { name });
                }

                if (commit) {
//                    db.execSQL(";commit;");
                    db.setTransactionSuccessful();
                    db.endTransaction();
                } else {
//                    db.execSQL(";rollback;");
                    db.endTransaction();
                }
            } catch (Throwable t) {
                Debug.e(t);
            }

            callback.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SQLiteDatabase db = SQLiteDatabase.create(null);


        db.execSQL("create table t(oid integer primary key autoincrement, name text);");


        db.execSQL(";begin transaction deferred;");
//        db.execSQL(";begin transaction deferred;");
        db.execSQL(";insert into t(name) values(?);", new Object[] { "0" });
        db.execSQL(";commit transaction;");

        final Cursor cursor = db.rawQuery("select * from t", null);
        try {
            while (cursor.moveToNext()) {
                Debug.i("oid: %d, name: %s", cursor.getLong(cursor.getColumnIndex("oid")), cursor.getString(cursor.getColumnIndex("name")));
            }
        } finally {
            cursor.close();
        }

        final Callback callback = new Callback() {

            int count = 0;

            @Override
            public void apply() {
                if (++count == 3) {
                    for (String t: new String[] { "1", "2", "3" }) {
                        final Cursor cursor = db.rawQuery("select * from t where name = ?", new String[] { t });
                        try {
                            Debug.i("table: %s, count: %d", t, cursor.getCount());
                        } finally {
                            cursor.close();
                        }
                    }
                }
            }
        };

        Debug.e("FIXED 3");

        final ExecutorService service = Executors.newFixedThreadPool(3);
        service.submit(new Run(db, "1", true, callback));
        service.submit(new Run(db, "2", false, callback));
        service.submit(new Run(db, "3", true, callback));

//        final ThreadLocal<String> transactions = new ThreadLocal<String>() {
//            @Override
//            protected String initialValue() {
//                return "Thread: " + Thread.currentThread() + ", Date: " + new Date();
//            }
//        };
    }
}
