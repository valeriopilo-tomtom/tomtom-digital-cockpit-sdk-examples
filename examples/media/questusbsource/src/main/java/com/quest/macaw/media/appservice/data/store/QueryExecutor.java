package com.quest.macaw.media.appservice.data.store;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueryExecutor {

    private static final String TAG = "QueryExecutor";

    private final Runnable mRunnableQuery;
    private ExecutorService mExecutorService;

    public QueryExecutor(Runnable runnable) {
        mRunnableQuery = runnable;
    }

    public void execute() {
        Log.i(TAG, "execute");
        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.execute(mRunnableQuery);
    }

    public void execute(Runnable runnable) {
        Log.i(TAG, "execute");
        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.execute(runnable);
    }

    public boolean isTerminated() {
        if (mExecutorService != null) {
            return mExecutorService.isTerminated();
        } else {
            Log.e(TAG, "mExecutorService is null");
            return true;
        }
    }

    public void shutdown() {
        Log.i(TAG, "shutdown");
        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        } else {
            Log.e(TAG, "mExecutorService is null");
        }
    }
}
