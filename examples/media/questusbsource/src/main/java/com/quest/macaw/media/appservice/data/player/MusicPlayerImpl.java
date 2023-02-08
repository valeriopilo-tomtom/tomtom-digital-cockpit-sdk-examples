package com.quest.macaw.media.appservice.data.player;

import android.app.Notification;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.quest.macaw.media.appservice.data.alexa.AlexaMediaSourceFactory;
import com.quest.macaw.media.appservice.data.interfaces.MusicPlayer;
import com.quest.macaw.media.appservice.data.store.MediaStoreAccessor;
import com.quest.macaw.media.common.SongInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;


public class MusicPlayerImpl implements MusicPlayer {

    private static final String TAG = "MusicPlayer";
    private static final String MEDIA_USER_AGENT = "media_player";

    private final Context mContext;
    private final MediaStoreAccessor mDataModel;
    private static final float VOLUME_MULTIPLIER_DUCK = 0.05f;
    private static final float VOLUME_MULTIPLIER_NORMAL = 1.0f;
    private final MediaSessionConnector mMediaSessionConnector;
    private SimpleExoPlayer mExoPlayer;
    private DefaultMediaSourceFactory mDefaultMediaSourceFactory;
    private AlexaMediaSourceFactory mAlexaMediaSourceFactory;
    private ConcatenatingMediaSource mConcatenatingMediaSource;
    private PlayerEventListener mPlayerEventListener;
    private List<MediaMetadataCompat> mMetadataPlaylist = new ArrayList<>();
    private List<SongInfo> mPlaylist = new ArrayList<>();
    private int mCurrentWindow = 0;
    private long mPlaybackPosition = 0L;
    private final AudioManager mAudioManager;
    private final AudioFocusRequest mAudioFocusRequest;
    private Handler mProgressHandler = new Handler(Looper.myLooper());
    private boolean mHasAudioFocus;
    private boolean mIsPlaying;

    private MediaPlayerListener mMediaPlayerListener;


    public MusicPlayerImpl(Context context, MediaSessionCompat mediaSessionCompat, MediaStoreAccessor dataModel) {
        mContext = context;
        mDataModel = dataModel;
        mMediaSessionConnector = new MediaSessionConnector(mediaSessionCompat);
        mMediaSessionConnector.setPlaybackPreparer(new MusicPlaybackPreparer());
        //mMediaSessionConnector.setQueueNavigator(new MusicQueueNavigator(mediaSessionCompat));
        initializePlayer(context);
        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mAudioFocusListener)
                .build();
       // mHasAudioFocus = requestAudioFocus();
        mMediaSessionConnector.setPlayer(mExoPlayer);

    }

    @Override
    public void prepare(List<SongInfo> songInfoList, boolean playWhenReady) {
        mPlaylist = songInfoList;
        mConcatenatingMediaSource = new ConcatenatingMediaSource();
        buildMediaSource(songInfoList);
        mExoPlayer.setMediaSource(mConcatenatingMediaSource);
        mExoPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
        mExoPlayer.setPlayWhenReady(playWhenReady);
        mExoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        mExoPlayer.setShuffleModeEnabled(false);
        mExoPlayer.prepare();
    }

    @Override
    public void updateMediaSource(List<SongInfo> songInfoList) {
        if (mConcatenatingMediaSource != null) {
            buildMediaSource(songInfoList.subList(mPlaylist.size(), songInfoList.size()));
        } else {
            Log.e(TAG, "mConcatenatingMediaSource is null");
        }
    }

    @Override
    public void play() {
        playMusic();
    }

    @Override
    public void pause() {
        mIsPlaying = false;
        mExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void shuffle(boolean isEnabled) {
        loop(!isEnabled);
        mExoPlayer.setShuffleModeEnabled(isEnabled);
    }

    @Override
    public void loop(boolean isEnabled) {
        mExoPlayer.setRepeatMode(isEnabled ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);
    }

    @Override
    public void nextTrac() {
        if (mExoPlayer.hasNextWindow()) {
            mExoPlayer.seekTo(mExoPlayer.getNextWindowIndex(), C.TIME_UNSET);
        }
    }

    @Override
    public void previousTrack() {
        if (mExoPlayer.hasPreviousWindow()) {
            mExoPlayer.seekTo(mExoPlayer.getPreviousWindowIndex(), C.TIME_UNSET);
        }
    }

    @Override
    public void favourite(boolean isEnabled) {
    }

    @Override
    public void seekTo(long progress) {
        mExoPlayer.seekTo(progress);
    }

    @Override
    public void loadMusic(String mediaId) {
        OptionalInt windowIndex = IntStream.range(0, mPlaylist.size())
                .filter(i -> mediaId.equals(mPlaylist.get(i).getMediaId()))
                .findFirst();
        Log.i(TAG, "loadMusic indexOpt: " + windowIndex);

        if (windowIndex.isPresent()) {
            mExoPlayer.seekTo(windowIndex.getAsInt(), C.TIME_UNSET);
            playMusic();
            //mExoPlayer.setPlayWhenReady(true);
        }
    }

    private boolean requestAudioFocus(){
        if(mAudioManager!=null && mAudioFocusRequest!=null){
            int result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            Log.i(TAG, "audio focus request result :"+  result);
            return result == AudioManager.AUDIOFOCUS_GAIN;
        }
        return false;
    }

    @Override
    public void release() {
        mExoPlayer.removeListener(mPlayerEventListener);
        mExoPlayer.release();
        mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
    }

    @Override
    public int getAudioSessionId() {
       return mExoPlayer.getAudioSessionId();
    }

    @Override
    public void prepare(Uri uri) {
        try {
            MediaSource mediaSource = mAlexaMediaSourceFactory.createHttpMediaSource(uri);
            mExoPlayer.setMediaSource(mediaSource);
            mExoPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
            mExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            mExoPlayer.setShuffleModeEnabled(false);
            mExoPlayer.prepare();
        } catch (Exception e) {
            Log.w(TAG, " Exception " + e.getMessage());
            mExoPlayer.stop();
            mMediaPlayerListener.processMediaError(e.getMessage());
        }
    }

    @Override
    public void stop() {
        mIsPlaying = false;
       mExoPlayer.stop();
       mExoPlayer.setPlayWhenReady(false);
    }

    private void playMusic(){
        Log.i(TAG, "Music playback started");
        if(mHasAudioFocus){
            mExoPlayer.setPlayWhenReady(true);
            mExoPlayer.setVolume(VOLUME_MULTIPLIER_NORMAL);
            mIsPlaying = true;
        } else {
            Log.i(TAG, "Audio focus lost, requesting for focus");
            mHasAudioFocus = requestAudioFocus();
            if(mHasAudioFocus){
                mExoPlayer.setPlayWhenReady(true);
                mExoPlayer.setVolume(VOLUME_MULTIPLIER_NORMAL);
                mIsPlaying = true;
            } else {
                Log.e(TAG, "Audio focus request failed");
                mIsPlaying = false;
            }
        }
    }

    @Override
    public void setVolume(float multiplier) {
       mExoPlayer.setVolume(multiplier);
    }

    @Override
    public void startOver() {
        mExoPlayer.seekTo(0);
    }

    private void initializePlayer(Context context) {
        mDefaultMediaSourceFactory = new DefaultMediaSourceFactory(context);
        mAlexaMediaSourceFactory = new AlexaMediaSourceFactory(context);
        mPlayerEventListener = new PlayerEventListener();

/*        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();*/

        mExoPlayer = new SimpleExoPlayer.Builder(context)
                .setMediaSourceFactory(mDefaultMediaSourceFactory)
                .setTrackSelector(new DefaultTrackSelector(context))
                .build();
        mExoPlayer.addListener(mPlayerEventListener);
    }

    private void resetPlayer() {
        mExoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        mExoPlayer.setPlayWhenReady(false);
        mExoPlayer.release();
        mExoPlayer = null;
        initializePlayer(mContext);
    }

    private MediaSource buildMediaSource(List<SongInfo> songInfoList) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, MEDIA_USER_AGENT));
        for (SongInfo songInfo : songInfoList) {
            mConcatenatingMediaSource.addMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(new MediaItem.Builder()
                            .setMediaId(songInfo.getMediaId())
                            .setUri(songInfo.getSongUri())
                            .build()));
        }
        return mConcatenatingMediaSource;
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focus) {
         Log.w(TAG , " onAudioFocusChange called with focus type: "+focus);
         Log.w(TAG , " mIsPlaying: "+ mIsPlaying);
            switch (focus) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mHasAudioFocus = true;
                    mExoPlayer.setVolume(VOLUME_MULTIPLIER_NORMAL);
                    if(mIsPlaying){
                        playMusic();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mHasAudioFocus = false;
                    mExoPlayer.setPlayWhenReady(false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mExoPlayer.setVolume(VOLUME_MULTIPLIER_DUCK);
                    break;
                default:
                    android.util.Log.e(TAG, "Unhandled audio focus type: " + focus);
            }
        }
    };

    private class PlayerEventListener implements Player.Listener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG, "onPlayerStateChanged ready: " + playWhenReady + " state: " + playbackState);
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onPlayerStateChanged(playWhenReady, playbackState);
            } else {
                Log.w(TAG , " mMediaPlayerListener is null");
            };
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Log.i(TAG, "onIsPlayingChanged....isPlaying: " + isPlaying);
            if (isPlaying) {
                startProgress();
            } else {
                stopProgress();
            }
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onPlaybackStarted(isPlaying);
            }
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Log.i(TAG, "onMediaItemTransition playWhenReady: " + playWhenReady + " reason: " + reason);
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            if (mediaItem != null) {
                Log.i(TAG, "onMediaItemTransition mediaItem: " + mediaItem.mediaId + " reason: " + reason);
                if (mMediaPlayerListener != null) {
                    mMediaPlayerListener.onSongChange(mediaItem.mediaId);
                }
            } else {
                Log.e(TAG, "onMediaItemTransition: mediaItem is null");
            }
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.i(TAG, "onShuffleModeEnabledChanged shuffleModeEnabled: " + shuffleModeEnabled);
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onShuffleModeChange(shuffleModeEnabled);
            }
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            Log.i(TAG, "onPlayerError: " + error);
            if(mMediaPlayerListener!=null){
                mMediaPlayerListener.onPlayerError(error.toString());
            }
        }

        private void startProgress() {
            mProgressHandler.postDelayed(progressRunnable, 500);
        }

        private void stopProgress() {
            mProgressHandler.removeCallbacks(progressRunnable);
        }
    }

    private class MusicPlaybackPreparer extends MediaSessionCompat.Callback implements MediaSessionConnector.PlaybackPreparer {


        @Override
        public long getSupportedPrepareActions() {
            Log.i(TAG, "getSupportedPrepareActions");
            return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
                    | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    | PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH
                    | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        }

        @Override
        public void onPrepare(boolean playWhenReady) {
            Log.i(TAG, "onPrepare");
        }

        @Override
        public void onPrepareFromMediaId(@NonNull String mediaId, boolean playWhenReady, @Nullable Bundle extras) {
            Log.i(TAG, "onPrepareFromMediaId ... mediaId: " + mediaId + " playWhenReady: " + playWhenReady);
            mMetadataPlaylist.clear();
            MediaMetadataCompat metadataCompat = mDataModel.getMetadata(mediaId);
            mMetadataPlaylist.add(metadataCompat);
            List<MediaSessionCompat.QueueItem> queueItems = mDataModel.getQueue();
            Log.i(TAG, "size: " + queueItems.size());
            for (MediaSessionCompat.QueueItem queueItem : queueItems) {
                Log.i(TAG, "getMediaId: " + queueItem.getDescription().getMediaId());
                assert queueItem.getDescription().getMediaId() != null;
                if (!queueItem.getDescription().getMediaId().equals(mediaId)) {
                    mMetadataPlaylist.add(mDataModel.getMetadata(queueItem.getDescription().getMediaId()));
                }
            }
            //prepare(mMetadataPlaylist, playWhenReady);
        }

        @Override
        public void onPrepareFromSearch(@NonNull String query, boolean playWhenReady, @Nullable Bundle extras) {
            Log.i(TAG, "onPrepareFromSearch");
        }

        @Override
        public void onPrepareFromUri(@NonNull Uri uri, boolean playWhenReady, @Nullable Bundle extras) {
            Log.i(TAG, "onPrepareFromUri");
        }

        @Override
        public boolean onCommand(@NonNull Player player, @NonNull ControlDispatcher controlDispatcher, @NonNull String command, @Nullable Bundle extras, @Nullable ResultReceiver cb) {
            Log.i(TAG, "onCommand");
            return false;
        }
    }

    private class MusicQueueNavigator extends TimelineQueueNavigator {

        public MusicQueueNavigator(MediaSessionCompat mediaSessionCompat) {
            super(mediaSessionCompat);
        }

        @NonNull
        @Override
        public MediaDescriptionCompat getMediaDescription(@NonNull Player player, int windowIndex) {
            Log.i(TAG, "getMediaDescription: " + windowIndex);
            return mMetadataPlaylist.get(windowIndex).getDescription();
        }
    }

    private class PlayerNotificationListener implements PlayerNotificationManager.NotificationListener {

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {

        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {

        }
    }

    @Override
    public void setOnProgressChangeListener(MediaPlayerListener mMediaPlayerListener) {
        this.mMediaPlayerListener = mMediaPlayerListener;
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayerListener != null) {
                long position = mExoPlayer.getCurrentPosition();
                long duration = mExoPlayer.getDuration();
                mMediaPlayerListener.onProgress(position, duration);
                mMediaPlayerListener.onProgressToAlexa(position, duration);
                mProgressHandler.removeCallbacks(MusicPlayerImpl.this.progressRunnable);
                mProgressHandler.postDelayed(MusicPlayerImpl.this.progressRunnable, 500);
            }
        }
    };

    public interface MediaPlayerListener {

        void onProgress(long progress, long duration);

        void onSongChange(String mediaId);

        void onPlaybackStarted(boolean isStarted);

        void onShuffleModeChange(boolean isShuffleEnabled);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onProgressToAlexa(long position, long duration);

        void processMediaError(String message);

        void onPlayerError(String errorMsg);
    }
}
