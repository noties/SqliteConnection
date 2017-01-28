package ru.noties.sqliteconnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class TransactionTestBase {

    protected abstract SqliteConnection getConnection();
    protected abstract Class<? extends Throwable> getSqliteException();

    private SqliteConnection mConnection;

    @Before
    public void before() {
        mConnection = getConnection();
    }

    @After
    public void after() {
        if (!mConnection.isClosed()) {
            throw new RuntimeException("Bad connection management. Connection is not closed");
        }
    }

    @Test
    public void multipleBegin() {
        try {
            Transaction.begin(mConnection)
                    .beginTransaction();
            assertTrue(false);
        } catch (Throwable t) {
            if (getSqliteException().equals(t.getClass())) {
                assertTrue(true);
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    @Test
    public void isInTransactionCommit() {
        final Transaction transaction = Transaction.begin(mConnection);
        assertTrue(transaction.isInTransaction());
        transaction.commit();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void isInTransactionRollback() {
        final Transaction transaction = Transaction.begin(mConnection);
        assertTrue(transaction.isInTransaction());
        transaction.rollback();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void commitClosesSavepoint() {
        final Transaction transaction = Transaction.begin(mConnection);
        transaction.savepoint("savepoint");
        assertTrue(transaction.isInTransaction());
        transaction.commit();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void rollbackClosesSavepoint() {
        final Transaction transaction = Transaction.begin(mConnection);
        transaction.savepoint("another");
        assertTrue(transaction.isInTransaction());
        transaction.rollback();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void savepointRelease() {
        final Transaction transaction = Transaction.begin(mConnection);
        transaction.savepoint("yo");
        assertTrue(transaction.isInTransaction());
        transaction.release("yo");
        assertTrue(transaction.isInTransaction());
        transaction.commit();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void savepointRollback() {
        final Transaction transaction = Transaction.begin(mConnection);
        transaction.savepoint("yes");
        assertTrue(transaction.isInTransaction());
        transaction.rollback("yes");
        assertTrue(transaction.isInTransaction());
        transaction.commit();
        assertFalse(transaction.isInTransaction());
    }

    @Test
    public void releaseSavepointNotPresent() {
        final Transaction transaction = Transaction.begin(mConnection);
        try {
            transaction.release("not_me");
            assertTrue(false);
        } catch (Throwable t) {
            if (getSqliteException().equals(t.getClass())) {
                assertTrue(true);
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    @Test
    public void rollbackSavepointNotPresent() {
        final Transaction transaction = Transaction.begin(mConnection);
        try {
            transaction.rollback("me_not");
            assertTrue(false);
        } catch (Throwable t) {
            if (getSqliteException().equals(t.getClass())) {
                assertTrue(true);
            } else {
                throw new RuntimeException(t);
            }
        }
    }
}
