/*
 * Copyright (c) 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quest.macaw.media.appservice.data.store;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.media.MediaBrowserServiceCompat;

import com.quest.macaw.media.appservice.data.utils.MediaConstant;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaStoreAccessor {

    private static final String TAG = "MediaStoreAccessor";
    private static final Uri[] ALL_AUDIO_URI = new Uri[]{
            //MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    };

    private static final String EXTERNAL = "external";
    private static final Uri ART_BASE_URI = Uri.parse("content://media/external_primary/audio/albumart");

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final List<MediaSessionCompat.QueueItem> mQueue = new ArrayList<>();

    private QueryExecutor mPendingQueryExecutor;


    public MediaStoreAccessor(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public void onQueryAllSongs(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onQueryAllSongs: " + parentId);
        QueryExecutor queryExecutor = new QueryBuilder()
                .setContentResolver(mContentResolver)
                .setResult(result)
                .setUri(ALL_AUDIO_URI)
                .setKeyColumn(BaseColumns._ID)
                .setTitleColumn(MediaStore.MediaColumns.TITLE)
                .setSubtitleColumn(MediaStore.MediaColumns.ALBUM_ARTIST)
                .setFlags(MediaBrowser.MediaItem.FLAG_PLAYABLE)
                .build();
        queryInBackground(result, queryExecutor);
    }

    public void onQueryByAlbum(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onQueryByAlbum: " + parentId);
        QueryBuilder queryBuilder = new QueryBuilder()
                .setContentResolver(mContentResolver)
                .setUri(ALL_AUDIO_URI)
                //.setColumns(new String[]{MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.ALBUM})
                .setResult(result);
        QueryExecutor queryExecutor = new QueryExecutor(() -> Query.getAll(queryBuilder));
        queryInBackground(result, queryExecutor);
    }

    public void onQueryByArtist(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onQueryByArtist: " + parentId);
        QueryExecutor query = new QueryBuilder()
                .setContentResolver(mContentResolver)
                .setResult(result)
                .setUri(ALL_AUDIO_URI)
                .setKeyColumn(AudioColumns.ARTIST_ID)
                .setTitleColumn(MediaStore.MediaColumns.ARTIST)
                .setSubtitleColumn(MediaStore.MediaColumns.ALBUM)
                .setFlags(MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
                .build();
        queryInBackground(result, query);
    }

    public void onQueryByGenre(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onQueryByGenre: " + parentId);
        QueryExecutor query = new QueryBuilder()
                .setContentResolver(mContentResolver)
                .setResult(result)
                .setUri(ALL_AUDIO_URI)
                .setKeyColumn(BaseColumns._ID)
                .setTitleColumn(MediaStore.Audio.GenresColumns.NAME)
                .setFlags(MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
                .build();
        queryInBackground(result, query);
    }

    /**
     * Note: This clears out the queue. You should have a local copy of the queue before calling
     * this method.
     */
    public void onQueryByKey(String lastCategory, String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onQueryByKey: " + parentId);
        mQueue.clear();
        QueryBuilder queryBuilder = new QueryBuilder()
                .setKeyColumn(BaseColumns._ID)
                .setContentResolver(mContentResolver)
                .setResult(result);

        if (MediaConstant.MediaBrowse.GENRES_ID.equals(lastCategory)) {
            // Genres come from a different table and don't use the where clause from the
            // usual media table so we need to have this condition.
            try {
                long id = Long.parseLong(parentId);
                queryBuilder.setUri(new Uri[]{
                        MediaStore.Audio.Genres.Members.getContentUri(EXTERNAL, id)});
            } catch (NumberFormatException e) {
                // This should never happen.
                Log.e(TAG, "Incorrect key type: " + parentId + ", sending empty result");
                result.sendResult(new ArrayList<>());
                return;
            }
        } else {
            String whereCluase = BaseColumns._ID + "= ? or "
                    + MediaStore.MediaColumns.ALBUM + "= ? or "
                    + AudioColumns.ARTIST_ID + " = ? or "
                    + MediaStore.MediaColumns.TITLE + " = ? or "
                    + MediaStore.MediaColumns.BUCKET_DISPLAY_NAME + " like ?";

            queryBuilder.setUri(ALL_AUDIO_URI)
                    .setWhereClause(whereCluase)
                    .setWhereArgs(new String[]{parentId, parentId, parentId, parentId, parentId});
        }

        QueryExecutor queryExecutor = queryBuilder.setKeyColumn(BaseColumns._ID)
                .setTitleColumn(MediaStore.MediaColumns.DISPLAY_NAME)
                .setSubtitleColumn(MediaStore.MediaColumns.ALBUM)
                .setFlags(MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                .setQueue(mQueue)
                .build();
        queryInBackground(result, queryExecutor);
    }

    private void queryInBackground(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result,
                                   QueryExecutor queryExecutor) {
        result.detach();

        if (mPendingQueryExecutor != null && !mPendingQueryExecutor.isTerminated()) {
            mPendingQueryExecutor.shutdown();
        }
        mPendingQueryExecutor = queryExecutor;
        queryExecutor.execute();
    }

    public List<MediaSessionCompat.QueueItem> getQueue() {
        return mQueue;
    }

    public MediaMetadataCompat getMetadata(String key) {
        Log.d(TAG, "getMetadata: " + key);
        Cursor cursor = null;
        MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder();
        try {
            for (Uri uri : ALL_AUDIO_URI) {
                cursor = mContentResolver.query(uri, null, BaseColumns._ID + " = ?",
                        new String[]{key}, null);
                if (cursor != null) {
                    int id = cursor.getColumnIndex(BaseColumns._ID);
                    int title = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
                    int artist = cursor.getColumnIndex(MediaStore.MediaColumns.ARTIST);
                    int album = cursor.getColumnIndex(MediaStore.MediaColumns.ALBUM);
                    int albumId = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
                    int duration = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);

                    while (cursor.moveToNext()) {
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                                cursor.getString(id));
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                                cursor.getString(title));
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                                cursor.getString(artist));
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                                cursor.getString(album));
                        metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                                cursor.getLong(duration));

                        String albumArt = null;

                        Uri albumArtUri = ContentUris.withAppendedId(ART_BASE_URI, cursor.getLong(albumId));
                        albumArtUri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_CONTENT)
                                .authority(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                                .path("/audio/albumart")
                                .build();
                        albumArtUri = ContentUris.withAppendedId(albumArtUri, cursor.getLong(albumId));


                        albumArtUri = ContentUris.withAppendedId(ART_BASE_URI, cursor.getLong(albumId));
                        //try {
                        //InputStream dummy = mResolver.openInputStream(albumArtUri);
                        albumArt = albumArtUri.toString();
                        //dummy.close();
                        /*} catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }*/
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArt);
                        Uri mediaUri = Uri.withAppendedPath(uri, "" + cursor.getString(id));
                        metadata.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri.toString());
                        break;
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return metadata.build();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                mContext.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
