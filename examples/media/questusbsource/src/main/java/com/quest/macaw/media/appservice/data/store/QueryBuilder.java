package com.quest.macaw.media.appservice.data.store;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

public class QueryBuilder {

    private MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> mResult;
    private String[] mColumns;
    private String mWhereClause;
    private String[] mWhereArgs;
    private String mKeyColumn;
    private String mTitleColumn;
    private String mSubtitleColumn;
    private Uri[] mUris;
    private int mFlags;
    private ContentResolver mResolver;
    private List<MediaSessionCompat.QueueItem> mQueue;

    public QueryBuilder setColumns(String[] columns) {
        mColumns = columns;
        return this;
    }

    public QueryBuilder setWhereClause(String whereClause) {
        mWhereClause = whereClause;
        return this;
    }

    public QueryBuilder setWhereArgs(String[] whereArgs) {
        mWhereArgs = whereArgs;
        return this;
    }

    public QueryBuilder setUri(Uri[] uris) {
        mUris = uris;
        return this;
    }

    public QueryBuilder setKeyColumn(String keyColumn) {
        mKeyColumn = keyColumn;
        return this;
    }

    public QueryBuilder setTitleColumn(String titleColumn) {
        mTitleColumn = titleColumn;
        return this;
    }

    public QueryBuilder setSubtitleColumn(String subtitleColumn) {
        mSubtitleColumn = subtitleColumn;
        return this;
    }

    public QueryBuilder setFlags(int flags) {
        mFlags = flags;
        return this;
    }

    public QueryBuilder setResult(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        mResult = result;
        return this;
    }

    public QueryBuilder setContentResolver(ContentResolver resolver) {
        mResolver = resolver;
        return this;
    }

    public QueryBuilder setQueue(List<MediaSessionCompat.QueueItem> queue) {
        mQueue = queue;
        return this;
    }

    public MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> getResult() {
        return mResult;
    }

    public String[] getColumns() {
        return mColumns;
    }

    public String getWhereClause() {
        return mWhereClause;
    }

    public String[] getWhereArgs() {
        return mWhereArgs;
    }

    public String getKeyColumn() {
        return mKeyColumn;
    }

    public String getTitleColumn() {
        return mTitleColumn;
    }

    public String getSubtitleColumn() {
        return mSubtitleColumn;
    }

    public Uri[] getUris() {
        return mUris;
    }

    public int getFlags() {
        return mFlags;
    }

    public ContentResolver getContentResolver() {
        return mResolver;
    }

    public List<MediaSessionCompat.QueueItem> getQueue() {
        return mQueue;
    }

    public QueryExecutor build() {
        if (mUris == null || mKeyColumn == null || mResolver == null ||
                mResult == null || mTitleColumn == null) {
            throw new IllegalStateException(
                    "uri, keyColumn, resolver, result and titleColumn are required.");
        }
        return new QueryExecutor(() -> Query.getByKey(this));
    }
}
