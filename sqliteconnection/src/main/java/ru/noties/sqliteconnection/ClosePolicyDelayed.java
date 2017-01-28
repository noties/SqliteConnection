package ru.noties.sqliteconnection;

import android.os.Handler;
import android.support.annotation.NonNull;

public class ClosePolicyDelayed implements ClosePolicy {

    // closes with specified delay
    // if new connection is requested, removes enqueued close operation

    private final Handler mHandler;
    private final long mDelay;

    private boolean mSelfCall;

    public ClosePolicyDelayed(long delay) {
        this(new Handler(), delay);
    }

    @SuppressWarnings("WeakerAccess")
    public ClosePolicyDelayed(@NonNull Handler handler, long delay) {
        mHandler = handler;
        mDelay = delay;
    }

    @Override
    public void onNewConnectionRequested(SqliteDataSource controller) {
        mHandler.removeCallbacksAndMessages(true);
    }

    @Override
    public boolean onAdditionalConnectionOpen(SqliteDataSource controller) {
        return true;
    }

    @Override
    public boolean onLastConnectionClose(final SqliteDataSource connectionController) {

        if (mSelfCall) {
            return true;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSelfCall = true;
                connectionController.close();
                mSelfCall = false;
            }
        }, mDelay);

        return false;
    }
}
