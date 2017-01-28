package ru.noties.sqliteconnection;

import android.database.Cursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class SqliteConnectionTestBase {

    // todo! tests:
    //      * connection never closes database ??
    //      * register/unregister/notifications for StateObserver (not throws whilst iterating /
    //          allows only one instance of StateObserver, unregister not present doesn't do anything /
    //          StateObserver receives correct sql and bindArgs, close notifies only once)
    //      * `query` operation and binding byte[] must throw
    //      * observable
    //      * closingPolicies -> move to dataSource test
    //      * all operations will notify StateObservers

    // MUST return dataSource with ClosePolicyImmediate (others should be tested somewhere else)
    protected abstract SqliteConnection getConnection();
    protected abstract boolean isDatabaseClosed(SqliteConnection connection);

    private SqliteConnection mConnection;

    @Before
    public void before() {
        mConnection = getConnection();
    }

    // if there was an exception executing test method
    // we won't receive its stacktrace, but instead... we will receive a RuntimeException
    // with bad connection management.
    // if something goes wrong -> try to comment-out/disable this method (@After) first
    @After
    public void after() {
        if (!mConnection.isClosed() || !isDatabaseClosed(mConnection)) {
            throw new RuntimeException("Bad connection management. Connection is not closed");
        }
    }

    @Test
    public void methodNameCorrect() {

        assertEquals("methodNameCorrect", methodName());

        // we do not use connection in this test, but we still need to close it
        mConnection.close();
    }

    @Test
    public void isOpen() {
        assertFalse(mConnection.isClosed());

        // close connection after a test
        mConnection.close();
    }

    @Test
    public void isClosed() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
    }

    @Test
    public void closingClosed() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
    }

    @Test
    public void bindingsCorrect() {

        final String table = methodName();
        assertFalse(tableExists(mConnection, table));

        mConnection.update("create table ${table}('bool' integer, 'int' integer, 'long' integer, 'float' real, 'double' real, 'string' text, 'blob' blob);")
                .bind("table", table)
                .execute();

        assertTrue(tableExists(mConnection, table));

        mConnection.insert("insert into ${table}(${columns}) values(?{bool}, ?{int}, ?{long}, ?{float}, ?{double}, ?{string}, ?{blob})")
                .bind("table", table)
                .bind("columns", "'bool', 'int', 'long', 'float', 'double', 'string', 'blob'")
                .bind("bool", true)
                .bind("int", 2)
                .bind("long", 3L)
                .bind("float", 4.F)
                .bind("double", 5.D)
                .bind("string", "6")
                .bind("blob", new byte[] { 7, 8, 9 })
                .execute();

        final Cursor cursor = mConnection.query("select * from ${table}")
                .bind("table", table)
                .execute();

        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);

        assertEquals(1, cursor.getInt(cursor.getColumnIndex("bool")));
        assertEquals(2, cursor.getInt(cursor.getColumnIndex("int")));
        assertEquals(3L, cursor.getLong(cursor.getColumnIndex("long")));
        assertEquals(4.F, cursor.getFloat(cursor.getColumnIndex("float")), .0005F);
        assertEquals(5.D, cursor.getDouble(cursor.getColumnIndex("double")), .0005D);
        assertEquals("6", cursor.getString(cursor.getColumnIndex("string")));
        assertArrayEquals(new byte[] { 7, 8, 9 }, cursor.getBlob(cursor.getColumnIndex("blob")));

        cursor.close();
        mConnection.close();
    }

    @Test
    public void closedExecute() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
        try {
            mConnection.execute("select 1;");
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void closedInsert() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
        try {
            mConnection.insert("insert into whatever").execute();
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void closedQuery() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
        try {
            mConnection.query("select 1;").execute();
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void closedUpdate() {
        assertFalse(mConnection.isClosed());
        mConnection.close();
        assertTrue(mConnection.isClosed());
        try {
            mConnection.update("just whatever").execute();
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void createTable() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key autoincrement, name text);");

        // assert table is created
        assertTrue(tableExists(mConnection, table));

        mConnection.close();
    }

    @Test
    public void createTableViaStatement() {

        final String table = methodName();
        assertFalse(tableExists(mConnection, table));

        mConnection.update("create table ${table}(oid integer, name text);")
                .bind("table", table)
                .execute();

        assertTrue(tableExists(mConnection, table));

        mConnection.close();
    }

    @Test
    public void insertSingle() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        // create a table
        mConnection.execute("create table " + table + "(oid integer primary key autoincrement, name text not null);");
        assertTrue(tableExists(mConnection, table));

        final long id = mConnection.insert("insert into ${table}(name) values(?{first})")
                .bind("table", table)
                .bind("first", "jajaja")
                .execute();

        assertTrue(id > 0L);

        final Cursor cursor = mConnection.query("select * from ${table}")
                .bind("table", table)
                .execute();

        try {
            assertTrue(cursor.moveToFirst());
            assertEquals(1, cursor.getCount());
            assertEquals(2, cursor.getColumnCount());
            assertEquals(id, cursor.getLong(cursor.getColumnIndexOrThrow("oid")));
            assertEquals("jajaja", cursor.getString(cursor.getColumnIndexOrThrow("name")));
        } finally {
            cursor.close();
        }

        mConnection.close();
    }

    @Test
    public void insertMultiple() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key autoincrement, name text not null);");
        assertTrue(tableExists(mConnection, table));

        final Set<String> set = new LinkedHashSet<String>(3) {{
            add("first");
            add("second");
            add("third");
        }};

        // multiple insert returns last inserted id
        final long lastId = mConnection.insert("insert into ${table}(name) values(?{value})")
                .batch(set, new BatchApply<Long, String>() {
                    @Override
                    public Long apply(Statement<Long> statement, String value) {
                        statement.clearBindings();
                        statement.bind("table", table);
                        statement.bind("value", value);
                        return statement.execute();
                    }
                })
                .execute();

        assertTrue(lastId > 0L);

        final Cursor cursor = mConnection.query("select * from ${table};")
                .bind("table", table)
                .execute();

        try {

            assertTrue(cursor.moveToFirst());
            assertEquals(3, cursor.getCount());
            assertEquals(2, cursor.getColumnCount());

            final int id = cursor.getColumnIndexOrThrow("oid");
            final int name = cursor.getColumnIndexOrThrow("name");

            final Set<String> names = new HashSet<>(set);

            boolean foundId = false;
            while (!cursor.isAfterLast()) {
                if (cursor.getLong(id) == lastId) {
                    foundId = true;
                }
                if (!names.remove(cursor.getString(name))) {
                    throw new RuntimeException("Cursor does not contain inserted values");
                }
                cursor.moveToNext();
            }
            assertTrue(foundId);

        } finally {
            cursor.close();
        }

        mConnection.close();
    }

    @Test
    public void insertInTransactionCommit() {

        // should insert & contain inserted values after commit is triggered (practically even before..)

        // as we are dealing with in-memory database, after it's closed all the data is deleted
        // so, we need especially keep open connection

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key, name text not null);");
        assertTrue(tableExists(mConnection, table));

        final Transaction transaction = Transaction.begin(mConnection);

        mConnection
                .insert("insert into ${table}(oid, name) values (?{oid}, ?{name});")
                .bind("table", table)
                .bind("oid", 34L)
                .bind("name", "Walter")
                .execute();

        // first check
        {
            final Cursor cursor = mConnection.query("select count(1) from ${table} where oid = ?{oid} and name = ?{name}")
                    .bind("table", table)
                    .bind("oid", 34L)
                    .bind("name", "Walter")
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getInt(0));
            } finally {
                cursor.close();
            }
        }

        // after commit nothing should change, we do another check if data is still present
        transaction.commit();

        // second check
        {
            final Cursor cursor = mConnection.query("select count(1) from ${table} where oid = ?{oid} and name = ?{name}")
                    .bind("table", table)
                    .bind("oid", 34L)
                    .bind("name", "Walter")
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getInt(0));
            } finally {
                cursor.close();
            }
        }

        mConnection.close();
    }

    @Test
    public void insertInTransactionRollback() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key, name text not null);");
        assertTrue(tableExists(mConnection, table));

        final Transaction transaction = Transaction.begin(mConnection);

        mConnection
                .insert("insert into ${table}(oid, name) values (?{oid}, ?{name});")
                .bind("table", table)
                .bind("oid", 154L)
                .bind("name", "Scott")
                .execute();

        // first check
        {
            final Cursor cursor = mConnection.query("select count(1) from ${table} where oid = ?{oid} and name = ?{name}")
                    .bind("table", table)
                    .bind("oid", 154L)
                    .bind("name", "Scott")
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getInt(0));
            } finally {
                cursor.close();
            }
        }

        transaction.rollback();

        // second check, should fail
        {
            final Cursor cursor = mConnection.query("select count(1) from ${table} where oid = ?{oid} and name = ?{name}")
                    .bind("table", table)
                    .bind("oid", 34L)
                    .bind("name", "Walter")
                    .execute();

            // 2 cases: Cursor can be empty, thus `moveToFirst()` returns false
            // or it can contain a row with the first value as `0`

            try {

                if (cursor.moveToFirst()) {
                    assertEquals(0, cursor.getInt(0));
                } else {
                    assertTrue(true);
                }

            } finally {
                cursor.close();
            }
        }

        mConnection.close();

    }

    @Test
    public void insertViaUpdateMethodSingle() {

        // for insert via update method (for testing) primary key of a table should not be autoincrement
        // just in case the method returns generated ids

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key, name text);");
        assertTrue(tableExists(mConnection, table));

        int changes = mConnection.update("insert into ${table}(oid, name) values (?{oid}, ?{name})")
                .bind("table", table)
                .bind("oid", 51L)
                .bind("name", "Santa")
                .execute();

        assertEquals(1, changes);

        mConnection.close();
    }

    @Test
    public void insertViaUpdateMethodMultiple() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key, name text);");
        assertTrue(tableExists(mConnection, table));

        final int changes = mConnection.update("insert into ${table}(oid, name) values(?{oid}, ?{name})")
                .batch(new String[]{"first", "second", "third"}, new BatchApply<Integer, String>() {
                    @Override
                    public Integer apply(Statement<Integer> statement, String value) {
                        statement.clearBindings();
                        statement.bind("table", table);
                        statement.bind("oid", value.hashCode());
                        statement.bind("name", value);
                        return statement.execute();
                    }
                })
                .execute();

        assertEquals(3, changes);

        mConnection.close();
    }

    @Test
    public void argumentChangesSqlStatement() {

        final String table = methodName();

        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(oid integer primary key autoincrement, name text);");

        assertTrue(tableExists(mConnection, table));

        mConnection.insert("insert into ${table}(name) values(?{value})")
                .batch(new String[]{"first", "second", "third"}, new BatchApply<Long, String>() {
                    @Override
                    public Long apply(Statement<Long> statement, String value) {
                        statement.clearBindings();
                        statement.bind("table", table);
                        statement.bind("value", value);
                        return statement.execute();
                    }
                })
                .execute();

        final StatementQuery query = mConnection.query("select ${column} from ${table}");
        {
            final Cursor cursor = query.bind("column", "oid")
                    .bind("table", table)
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getColumnCount());
                assertEquals(0, cursor.getColumnIndex("oid"));
            } finally {
                cursor.close();
            }
        }

        {
            final Cursor cursor = query.bind("column", "name")
                    .bind("table", table)
                    .execute();
            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(1, cursor.getColumnCount());
                assertEquals(0, cursor.getColumnIndex("name"));
            } finally {
                cursor.close();
            }
        }

        mConnection.close();
    }

    @Test
    public void insertSqlStatementChanged() {

        // sql statement is formatted, so it should be changed

        final String table0 = methodName();
        final String table1 = "_" + table0;

        assertFalse(tableExists(mConnection, table0));
        assertFalse(tableExists(mConnection, table1));

        mConnection.execute("create table " + table0 + "(id0 integer primary key autoincrement, name0 text);");
        mConnection.execute("create table " + table1 + "(id1 integer primary key autoincrement, name1 text);");

        assertTrue(tableExists(mConnection, table0));
        assertTrue(tableExists(mConnection, table1));

        // we will reuse the insert statement
        final Statement<Long> insert = mConnection.insert("insert into ${table}(${column}) values(?{value})");

        // table0
        {
            insert.clearBindings();
            insert.bind("table", table0);
            insert.bind("column", "name0");
            insert.bind("value", "zero");
            insert.execute();

            final Cursor cursor = mConnection.query("select * from ${table}")
                    .bind("table", table0)
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(2, cursor.getColumnCount());
                assertTrue(cursor.getColumnIndex("id0") > -1);
                assertTrue(cursor.getColumnIndex("name0") > -1);
                assertEquals("zero", cursor.getString(cursor.getColumnIndex("name0")));
            } finally {
                cursor.close();
            }
        }

        // table1
        {
            insert.clearBindings();
            insert.bind("table", table1);
            insert.bind("column", "name1");
            insert.bind("value", "one");
            insert.execute();

            final Cursor cursor = mConnection.query("select * from ${table}")
                    .bind("table", table1)
                    .execute();

            try {
                assertTrue(cursor.moveToFirst());
                assertEquals(2, cursor.getColumnCount());
                assertTrue(cursor.getColumnIndex("id1") > -1);
                assertTrue(cursor.getColumnIndex("name1") > -1);
                assertEquals("one", cursor.getString(cursor.getColumnIndex("name1")));
            } finally {
                cursor.close();
            }
        }

        mConnection.close();
    }

    @Test
    public void insertSqlStatementChangedBatch() {

        final String table = methodName();
        assertFalse(tableExists(mConnection, table));

        mConnection.execute("create table " + table + "(first text, second text);");
        assertTrue(tableExists(mConnection, table));

        final class Pair {
            final String column;
            final String value;
            Pair(String column, String value) {
                this.column = column;
                this.value = value;
            }
        }

        final List<Pair> list = new ArrayList<Pair>() {{
            add(new Pair("first", "name"));
            add(new Pair("second", "surname"));
        }};

        mConnection.insert("insert into ${table}(${column}) values(?{value});")
                .batch(list, new BatchApply<Long, Pair>() {
                    @Override
                    public Long apply(Statement<Long> statement, Pair value) {
                        statement.clearBindings();
                        statement.bind("table", table);
                        statement.bind("column", value.column);
                        statement.bind("value", value.value);
                        return statement.execute();
                    }
                })
                .execute();

        final StatementQuery query = mConnection.query("select * from ${table} where ${column} is not null");

        {
            final Cursor cursor = query
                    .bind("table", table)
                    .bind("column", "first")
                    .execute();
            try {
                assertTrue(cursor.moveToFirst());
                assertEquals("name", cursor.getString(cursor.getColumnIndex("first")));
                assertNull(cursor.getString(cursor.getColumnIndex("second")));
            } finally {
                cursor.close();
            }
        }

        query.clearBindings();

        {
            final Cursor cursor = query
                    .bind("table", table)
                    .bind("column", "second")
                    .execute();
            try {
                assertTrue(cursor.moveToFirst());
                assertEquals("surname", cursor.getString(cursor.getColumnIndex("second")));
                assertNull(cursor.getString(cursor.getColumnIndex("first")));
            } finally {
                cursor.close();
            }
        }

        mConnection.close();
    }

    @Test
    public void updateExisting() {

        final String table = methodName();
        assertFalse(tableExists(mConnection, table));

        mConnection.update("create table ${table}(id integer, name text);")
                .bind("table", table)
                .execute();

        mConnection.insert("insert into ${table}(id, name) values(?{id}, ?{name})")
                .bind("table", table)
                .bind("id", 23)
                .bind("name", "twenty three")
                .execute();

        {
            // validate that the inserted data is present
            final Cursor cursor = mConnection.query("select * from ${table}")
                    .bind("table", table)
                    .execute();

            assertTrue(cursor.moveToFirst());
            assertEquals(1, cursor.getCount());
            assertEquals(2, cursor.getColumnCount());
            assertEquals(23, cursor.getInt(cursor.getColumnIndex("id")));
            assertEquals("twenty three", cursor.getString(cursor.getColumnIndex("name")));
            cursor.close();
        }

        final int updated = mConnection.update("update ${table} set id = ?{id}, name = ?{name} where id = ?{id_current};")
                .bind("table", table)
                .bind("id_current", 23)
                .bind("id", 56)
                .bind("name", "fifty six")
                .execute();

        assertEquals(1, updated);

        {
            final Cursor cursor = mConnection.query("select * from ${table};")
                    .bind("table", table)
                    .execute();

            assertTrue(cursor.moveToFirst());
            assertEquals(1, cursor.getCount());
            assertEquals(2, cursor.getColumnCount());
            assertEquals(56, cursor.getInt(cursor.getColumnIndex("id")));
            assertEquals("fifty six", cursor.getString(cursor.getColumnIndex("name")));
            cursor.close();
        }

        mConnection.close();
    }

//    @Test
//    public void simpleBindIgnoredIfBatchIsPresent() {
//        // batch has higher priority
//        throw null;
//    }

//
//    @Test
//    public void queryStatementBoundNullArgument() {
//        // should fail as it doesn't make sense to bind NULL arguments
//        throw null;
//    }
//
//    @Test
//    public void query() {
//        throw null;
//    }
//
//    @Test
//    public void queryWithJoin() {
//        throw null;
//    }

    private static boolean tableExists(SqliteConnection connection, String table) {

        final boolean out;

        final Cursor cursor = connection.query("select count(1) as count from sqlite_master where type = 'table' and name = ?{table}")
                .bind("table", table)
                .execute();

        try {
            if (cursor.moveToFirst()) {
                out = cursor.getInt(cursor.getColumnIndex("count")) > 0;
            } else {
                out = false;
            }
        } finally {
            cursor.close();
        }

        return out;
    }

    private static String methodName() {
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String name = null;
        for (StackTraceElement element: elements) {
            name = element.getMethodName();
            if ("getStackTrace".equals(name)
                    || "getThreadStackTrace".equals(name)
                    || "methodName".equals(name)) {
                continue;
            }
            break;
        }
        return name;
    }
}
