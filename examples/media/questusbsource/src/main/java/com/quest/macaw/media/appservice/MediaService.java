package com.quest.macaw.media.appservice;

import static com.quest.macaw.media.appservice.data.utils.MediaConstant.MediaBrowse.ALBUMS_ID;
import static com.quest.macaw.media.appservice.data.utils.MediaConstant.MediaBrowse.ARTISTS_ID;
import static com.quest.macaw.media.appservice.data.utils.MediaConstant.MediaBrowse.MEDIA_ROOT_ID;
import static com.quest.macaw.media.appservice.data.utils.MediaConstant.MediaBrowse.SONGS_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.quest.macaw.media.appservice.data.EqualizerManager;
import com.quest.macaw.media.appservice.data.clusterHandler.MacawClusterHandler;
import com.quest.macaw.media.appservice.data.MediaServiceInterface;
import com.quest.macaw.media.appservice.data.MediaServiceManager;
import com.quest.macaw.media.appservice.data.interfaces.MusicPlayer;
import com.quest.macaw.media.appservice.data.player.MusicPlayerImpl;
import com.quest.macaw.media.appservice.data.store.MediaStoreAccessor;
import com.quest.macaw.media.appservice.data.utils.MediaConstant;
import com.quest.macaw.media.appservice.data.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MediaService extends MediaBrowserServiceCompat {

    private static final String TAG = "MediaPlaybackService";
    private static final String MEDIA_SESSION_TAG = "MusicService";

    static final String ACTION_PLAY = "com.android.car.media.localmediaplayer.ACTION_PLAY";
    static final String ACTION_PAUSE = "com.android.car.media.localmediaplayer.ACTION_PAUSE";
    static final String ACTION_NEXT = "com.android.car.media.localmediaplayer.ACTION_NEXT";
    static final String ACTION_PREV = "com.android.car.media.localmediaplayer.ACTION_PREV";

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mBuilder;
    private MusicPlayer mMusicPlayer;
    private MediaStoreAccessor mDataModel;
    private List<MediaBrowserCompat.MediaItem> mRootItems = new ArrayList<>();
    private String mLastCategory;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        mDataModel = new MediaStoreAccessor(this);
        addRootItems();
        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(getApplicationContext(), MEDIA_SESSION_TAG);
        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mBuilder.build());
        // MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
        });
        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());
        mMusicPlayer = new MusicPlayerImpl(getApplicationContext(), mMediaSession, mDataModel);
        MediaServiceManager mediaServiceManager = new MediaServiceManager(getApplicationContext(), mMusicPlayer);
        MacawClusterHandler macawClusterHandler = new MacawClusterHandler(getApplicationContext(), mediaServiceManager);
        /*String CHANNEL_ID = "my_channel_01";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build();

        startForeground(1, notification);*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        Log.d(TAG, "onGetRoot: ");
        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MediaConstant.MediaBrowse.EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    @Override
    public void onLoadChildren(final String parentMediaId,
                               @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren: " + parentMediaId);
        switch (parentMediaId) {
            case MEDIA_ROOT_ID:
                result.sendResult(mRootItems);
                mLastCategory = parentMediaId;
                break;
            case SONGS_ID:
                mDataModel.onQueryAllSongs(parentMediaId, result);
                mLastCategory = parentMediaId;
                break;
            case ALBUMS_ID:
                mDataModel.onQueryByAlbum(parentMediaId, result);
                mLastCategory = parentMediaId;
                break;
            case ARTISTS_ID:
                mDataModel.onQueryByArtist(parentMediaId, result);
                mLastCategory = parentMediaId;
                break;
            /*case GENRES_ID:
                mDataModel.onQueryByGenre(parentMediaId, result);
                mLastCategory = parentMediaId;
                break;*/
            default:
                mDataModel.onQueryByKey(mLastCategory, parentMediaId, result);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicPlayer.release();
    }

    private boolean allowBrowsing(String clientPackageName, int clientUid) {
        if (!TextUtils.isEmpty(clientPackageName) && clientPackageName.equals("com.quest.macaw.media")) {
            return true;
        }
        return true;
    }

    private void addRootItems() {
        Log.d(TAG, "addRootItems: ");
        MediaDescriptionCompat allSongs = new MediaDescriptionCompat.Builder()
                .setMediaId(SONGS_ID)
                .setTitle(getString(R.string.all_songs))
                .setIconUri(Utils.getUriForResource(this, R.drawable.ic_folder))
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(allSongs, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat albums = new MediaDescriptionCompat.Builder()
                .setMediaId(ALBUMS_ID)
                .setTitle(getString(R.string.albums_title))
                .setIconUri(Utils.getUriForResource(this, R.drawable.ic_album))
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(albums, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat artists = new MediaDescriptionCompat.Builder()
                .setMediaId(ARTISTS_ID)
                .setTitle(getString(R.string.artists_title))
                .setIconUri(Utils.getUriForResource(this, R.drawable.ic_artist))
                .build();
        mRootItems.add(new MediaBrowserCompat.MediaItem(artists, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
    }
}

