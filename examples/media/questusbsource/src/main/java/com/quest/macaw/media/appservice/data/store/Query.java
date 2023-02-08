package com.quest.macaw.media.appservice.data.store;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.quest.macaw.media.common.SongInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {

    private static final String TAG = "Query";

    private Query() {
        //Not used.
    }

    public static void getByKey(QueryBuilder queryBuilder) {
        List<MediaBrowserCompat.MediaItem> results = new ArrayList<>();
        Cursor cursor = null;
        for (Uri uri : queryBuilder.getUris()) {
            try {
                String orderBy = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                cursor = queryBuilder
                        .getContentResolver()
                        .query(uri, queryBuilder.getColumns(), queryBuilder.getWhereClause(),
                                queryBuilder.getWhereArgs(), orderBy);

                if (cursor != null) {
                    getKeyData(cursor, queryBuilder, results);
                } else {
                    Log.e(TAG, "cursor is null");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to execute query " + e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        queryBuilder.getResult().sendResult(results);
    }

    public static void getAll(QueryBuilder queryBuilder) {
        Set<Pair<String, String>> files = new HashSet<>();
        Cursor cursor = null;
        for (Uri uri : queryBuilder.getUris()) {
            try {
                String orderBy = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
                cursor = queryBuilder.getContentResolver().query(uri, queryBuilder.getColumns(), null, null, orderBy + " ASC");
                if (cursor != null) {
                    getAllData(cursor, files);
                } else {
                    Log.e(TAG, "cursor is null");
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Failed to execute query: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        List<MediaBrowserCompat.MediaItem> results = new ArrayList<>();
        for (Pair<String, String> file : files) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(file.first + "%")  // Used in a like query.
                    .setTitle(file.first)
                    .setSubtitle(file.second)
                    .build();
            results.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        }
        queryBuilder.getResult().sendResult(results);
    }

    private static void getKeyData(Cursor cursor, QueryBuilder queryBuilder, List<MediaBrowserCompat.MediaItem> results) {
        int idx = 0;
        int keyColumn = cursor.getColumnIndex(queryBuilder.getKeyColumn());
        int titleColumn = cursor.getColumnIndex(queryBuilder.getTitleColumn());
        int subtitleColumn = -1;

        if (queryBuilder.getSubtitleColumn() != null) {
            subtitleColumn = cursor.getColumnIndex(queryBuilder.getSubtitleColumn());
        }

        while (cursor.moveToNext()) {
            String mediaId = cursor.getString(keyColumn);
            String title = cursor.getString(titleColumn);
            String subtitle = "";
            MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                    .setMediaId(mediaId)
                    .setTitle(title);

            if (subtitleColumn != -1) {
                subtitle = cursor.getString(subtitleColumn);
                builder.setSubtitle(subtitle);
            }

            MediaDescriptionCompat description = builder.build();
            if (!TextUtils.isEmpty(description.getMediaId())) {
                MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat
                        .MediaItem(description, queryBuilder.getFlags());
                results.add(mediaItem);
                if (queryBuilder.getQueue() == null) {
                    List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
                    queryBuilder.setQueue(queue);
                }
                Log.i(TAG, "added  to queue");  // Stack trace is noisy.
                queryBuilder.getQueue().add(new MediaSessionCompat.QueueItem(description, idx));
            }
            idx++;
        }
    }

    private static void getAllData(Cursor cursor, Set<Pair<String, String>> files) {
        int nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
        int albumColumn = cursor.getColumnIndex(MediaStore.MediaColumns.ALBUM);
        while (cursor.moveToNext()) {
            String folder = cursor.getString(nameColumn);
            String album = cursor.getString(albumColumn);
            if (TextUtils.isEmpty(album)) {
                album = "";
            }
            Pair<String, String> file = Pair.create(folder, album);
            files.add(file);
        }
    }

    public static List<SongInfo> getAll(ContentResolver contentResolver) {
        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            String orderBy = MediaStore.MediaColumns.TITLE;
            cursor = contentResolver.query(uri, null, null, null, orderBy + " ASC");
            if (cursor != null) {
                return getAllSongs(cursor);
            } else {
                Log.e(TAG, "cursor is null");
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed to execute query: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new ArrayList<>();
    }

    private static List<SongInfo> getAllSongs(Cursor cursor) {
        int keyColumn = cursor.getColumnIndex(BaseColumns._ID);
        int titleColumn = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
        int albumColumn = cursor.getColumnIndex(MediaStore.MediaColumns.ALBUM);
        int artistColumn = cursor.getColumnIndex(MediaStore.MediaColumns.ARTIST);
        int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
        int durationColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);
        int displayNameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);

        List<SongInfo> songInfoList = new ArrayList<>();
        while (cursor.moveToNext()) {
            SongInfo songInfo = new SongInfo();
            songInfo.setMediaId(cursor.getString(keyColumn));
            songInfo.setTitle(cursor.getString(titleColumn));
            songInfo.setAlbumName(cursor.getString(albumColumn));
            songInfo.setArtistName(cursor.getString(artistColumn));
            songInfo.setAlbumId(cursor.getLong(albumIdColumn));
            songInfo.setDuration(cursor.getLong(durationColumn));
            songInfo.setDisplayName(cursor.getString(displayNameColumn));
            songInfo.setCoverArtUrl(Uri.withAppendedPath(MediaStore.Audio.Media
                            .getContentUri(MediaStore.VOLUME_INTERNAL),
                    "" + cursor.getString(keyColumn)).toString());
            songInfo.setSongUri(Uri
                    .withAppendedPath(MediaStore.Audio.Media
                                    .getContentUri(MediaStore.VOLUME_INTERNAL),
                            "" + cursor.getString(keyColumn)));
            songInfo.setIsLocalPath(false);

            songInfoList.add(songInfo);
        }
        return songInfoList;
    }
}
