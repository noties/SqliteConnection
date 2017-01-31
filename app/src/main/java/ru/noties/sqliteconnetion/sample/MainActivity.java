package ru.noties.sqliteconnetion.sample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import ru.noties.debug.Debug;
import ru.noties.debug.out.AndroidLogDebugOutput;
import ru.noties.sqliteconnection.BatchApply;
import ru.noties.sqliteconnection.ConnectionHandler;
import ru.noties.sqliteconnection.SqliteConnection;
import ru.noties.sqliteconnection.SqliteDataSource;
import ru.noties.sqliteconnection.SqliteDataSourceFactory;
import ru.noties.sqliteconnection.Statement;
import ru.noties.sqliteconnection.StatementQuery;
import ru.noties.sqliteconnection.system.ConnectionHandlerSystem;
import ru.noties.sqliteconnection.utils.Provider;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SQLite system (stock Android)
        show(new Provider<SQLiteDatabase>() {
            @Override
            public SQLiteDatabase provide() {
                // memory database
                return SQLiteDatabase.create(null);
            }
        }, new ConnectionHandlerSystem());
    }

    private static class Data {

        long oid;
        String name;
        long time;

        Data(String name) {
            this.name = name;
        }

        Data(String name, long time) {
            this.name = name;
            this.time = time;
        }

        Data(long oid, String name, long time) {
            this.oid = oid;
            this.name = name;
            this.time = time;
        }
    }

    // should not be re-used between different queries (as column order might change)
    private static class DataRowMapper implements StatementQuery.RowMapper<Data> {

        private static class Indexes {
            final int oid;
            final int name;
            final int time;
            Indexes(Cursor cursor) {
                this.oid = cursor.getColumnIndex("oid");
                this.name = cursor.getColumnIndex("name");
                this.time = cursor.getColumnIndex("time");
            }
        }

        private Indexes mIndexes;

        @Override
        public Data map(Cursor cursor) {
            if (mIndexes == null) {
                mIndexes = new Indexes(cursor);
            }
            return new Data(
                    cursor.getLong(mIndexes.oid),
                    cursor.getString(mIndexes.name),
                    cursor.getLong(mIndexes.time)
            );
        }

    }

    private static <DB> void show(Provider<DB> provider, ConnectionHandler<DB> connectionHandler) {

        // the main class to execute sqlite commands is SqliteConnection
        // it can be obtained by SqliteDataSource

        // dataSource is not intended to be closed (even though there is a public `close` method)
        // calling this method might cause errors
        final SqliteDataSource dataSource = SqliteDataSourceFactory.create(provider, connectionHandler);

        final SqliteConnection connection = dataSource.open();
        // we can register StateObserver to be notified about SQL executed
        connection.registerStateObserver(new SqliteConnection.StateObserver() {
            @Override
            public void onExecute(SqliteConnection connection, String sql, Object[] bindArgs) {
                Debug.i("sql: %s, args: %s", sql, Arrays.toString(bindArgs));
            }

            @Override
            public void onClosed(SqliteConnection connection) {
                // it's safe unregister here
                connection.unregisterStateObserver(this);
            }
        });

        // okay, let's create a table with `raw` execution command (that returns nothing)
        connection.execute("create table if not exists tbl(oid integer primary key autoincrement, name text, time integer default 0);");

        // let's add some data
        // insert a single row
        // we are using SqlStateBuilder underneath, so we are using binding arguments
        // there 2 types of them: `${}` & `?{}`
        final long id = connection.insert("insert into ${table}(name) values(?{name_value})")
                .bind("table", "tbl")
                .bind("name_value", "First")
                .execute();

        // let's insert multiple rows
        final List<Data> list = Arrays.asList(
                new Data("Second", 333L),
                new Data("Third", 81),
                new Data("Forth")
        );

        // when batch is used insert statement will return last inserted row id
        connection.insert("insert into ${table}(name, time) values(?{name_value}, ?{time_value});")
                .bind("table", "tbl")
                .batch(list, new BatchApply<Long, Data>() {
                    @Override
                    public Long apply(Statement<Long> statement, Data value) {
                        statement.bind("name_value", value.name);
                        statement.bind("time_value", value.time);
                        // here statement returns correct row id for this row
                        // if there is a need we can store it here
                        return statement.execute();
                    }
                })
                .execute();

        // query operation
        final List<Data> fromDb = connection.query("select * from ${table};")
                .bind("table", "tbl")
                .map(new DataRowMapper())
                .asList()
                .execute();

        for (Data data: fromDb) {
            Debug.i("id: %d, name: %s, time: %d", data.oid, data.name, data.time);
        }

        // let's update all columns that have no time column set
        final int updated = connection.update("update ${table} set time = ?{time_value} where time = 0;")
                .bind("table", "tbl")
                .bind("time_value", 123456L)
                .execute();

        Debug.i("updated: %d", updated);

        // let's query again
        final List<Data> updatedFromDb = connection.query("select * from ${table};")
                .bind("table", "tbl")
                .map(new DataRowMapper())
                .asList()
                .execute();

        for (Data data: updatedFromDb) {
            Debug.i("id: %d, name: %s, time: %d", data.oid, data.name, data.time);
        }

        // delete operations are executed via `update` method
        final int deleted = connection.update("delete from ${table};")
                .bind("table", "tbl")
                .execute();

        Debug.i("deleted: %d", deleted);

        connection.query("select * from ${table};")
                .bind("table", "tbl")
                .toObservable()
                .subscribe(new Subscriber<Cursor>() {
                    @Override
                    public void onCompleted() {
                        connection.close();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Debug.e(e);
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        Debug.i("cursor count: %d", cursor.getCount());
                        cursor.close();
                    }
                });
    }
}
