package com.quest.macaw.media.appservice.data;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.quest.macaw.media.appservice.data.interfaces.MusicPlayer;
import com.quest.macaw.media.appservice.data.interfaces.OnSourceStateChangeListener;
import com.quest.macaw.media.appservice.data.player.MusicPlayerImpl;
import com.quest.macaw.media.appservice.data.store.QueryExecutor;
import com.quest.macaw.media.appservice.data.utils.Utils;
import com.quest.macaw.media.common.IAlexaMediaServiceCallback;
import com.quest.macaw.media.common.IMediaServiceCallback;
import com.quest.macaw.media.common.MediaEqualizerPreset;
import com.quest.macaw.media.common.MediaEqualizerSettings;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.MediaMetaData;
import com.quest.macaw.media.common.MediaSettings;
import com.quest.macaw.media.common.SongInfo;
import com.quest.macaw.media.common.SourceInfo;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

public class MediaServiceManager implements MusicPlayerImpl.MediaPlayerListener, OnSourceStateChangeListener {

    private static final String TAG = "MediaServiceManager";

    private final Context mContext;
    private final MusicPlayer mMusicPlayer;
    private final Handler mPlayerHandler;
    private final RemoteCallbackList<IMediaServiceCallback> mCallbackList = new RemoteCallbackList<>();
    private final RemoteCallbackList<IAlexaMediaServiceCallback> mAlexaMediaCallbackList = new RemoteCallbackList<>();
    private final Set<Integer> mAppWidgetId = new HashSet<>();
    private final SourceManager mSourceManager;
    private MediaMetaData mMediaMetaData;
    private MediaEqualizerPreset mMediaEqualizerPreset;
    private final SettingsManager mSettingsManager;
    private EqualizerManager mEqualizerManager;
    private MediaEqualizerSettings mMediaEqualizerSettings;
    private boolean playWhenReady = false;

    public MediaServiceManager(Context context, MusicPlayer musicPlayer) {
        mContext = context;
        mMusicPlayer = musicPlayer;
        mSettingsManager = new SettingsManager(context);
        mEqualizerManager = new EqualizerManager(musicPlayer,context);
        mSourceManager = SourceManager.getInstance(mContext, this);
        musicPlayer.setOnProgressChangeListener(this);
        mPlayerHandler = new Handler(Looper.getMainLooper(), mHandlerCallback);
        registerReceiver();
    }

    public void preparePlayer(List<SongInfo> songInfoList) {
        mMediaMetaData = new MediaMetaData();
        mMediaMetaData.setSongInfoList(songInfoList);
        mMusicPlayer.prepare(mMediaMetaData.getSongInfoList(), playWhenReady);
    }

    public void onControlAction(int controlAction) {

        if (mSourceManager.getActiveSource().getType().equals(MediaInterfaceConstant.SourceType.ALEXA)) {
            sendControlAction(controlAction);
        } else if (controlAction == MediaInterfaceConstant.MEDIA_CONTROL_ACTION_SHUFFLE_LOOP) {
            if (mMediaMetaData != null) {
                Message message = new Message();
                message.what = controlAction;
                message.arg1 = mMediaMetaData.isIsShuffleEnabled() ? 0 : 1;
                mPlayerHandler.sendMessage(message);
            }
        } else {
            mPlayerHandler.sendEmptyMessage(controlAction);
        }
    }

    public void loadMusic(String mediaId) {
        Message message = new Message();
        message.what = MediaInterfaceConstant.MEDIA_CONTROL_ACTION_SONG_CHANGE;
        Bundle bundle = new Bundle();
        bundle.putString(MediaInterfaceConstant.MEDIA_ID, mediaId);
        message.setData(bundle);
        mPlayerHandler.sendMessage(message);
    }

    public void seekTo(int progress) {
        Message message = new Message();
        message.what = MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PROGRESS;
        message.arg1 = progress;
        mPlayerHandler.sendMessage(message);
    }

    public MediaMetaData getMediaMetaData() {
        Log.d(TAG, "getMetaData");
        return mMediaMetaData;
    }

    public void updateMediaMetaData() {
        if (mMediaMetaData != null) {
            synchronized (mCallbackList) {
                int n = mCallbackList.beginBroadcast();
                Log.i(TAG, "getMediaMetaData callback count: " + n);
                try {
                    for (int i = 0; i < n; i++) {
                        Log.i(TAG, ">getMetaData: " + i);
                        mCallbackList.getBroadcastItem(i).updateSongInfo(mMediaMetaData);
                        Log.i(TAG, "<getMetaData: " + i);
                    }
                } catch (RemoteException ex) {
                    Log.i(TAG, "updateMediaMetaData: " + ex.getMessage());
                } finally {
                    mCallbackList.finishBroadcast();
                }
            }
        } else {
            Log.e(TAG, "getMediaMetaData mMediaMetaData is null");
        }
    }

    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_SHUFFLE_LOOP:
                    mMusicPlayer.shuffle(msg.arg1 == 1);
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PREVIOUS:
                    mMusicPlayer.previousTrack();
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PLAY:
                    mMusicPlayer.play();
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PAUSE:
                    mMusicPlayer.pause();
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_NEXT:
                    mMusicPlayer.nextTrac();
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_FAVORITE:
                    updateFavorite();
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_SONG_CHANGE:
                    mMusicPlayer.loadMusic(msg.getData().getString(MediaInterfaceConstant.MEDIA_ID));
                    break;
                case MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PROGRESS:
                    mMusicPlayer.seekTo(msg.arg1);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void sendControlAction(int action) {
        synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "sendControlAction callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">sendControlAction: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).sendControlAction(action);
                    Log.i(TAG, "<sendControlAction: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }

    }

    private void unregisterCallback(boolean isFromAlexa) {
        synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "unregisterCallback callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">unregisterCallback: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).unregisterCallback(isFromAlexa);
                    Log.i(TAG, "<unregisterCallback: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }
    }

    @Override
    public void onProgress(long progress, long duration) {
        synchronized (mCallbackList) {
            int n = mCallbackList.beginBroadcast();
            Log.d(TAG, "onProgress callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    mCallbackList.getBroadcastItem(i).onProgressChange(progress, duration);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, "onProgress: " + ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }

    @Override
    public void onSongChange(String mediaId) {
        synchronized (mCallbackList) {
            List<SongInfo> songInfoList = mMediaMetaData.getSongInfoList();
            if (songInfoList != null && !songInfoList.isEmpty()
                    && mSourceManager.getActiveSource().getType() != MediaInterfaceConstant.SourceType.ALEXA) {
                SongInfo songInfo = songInfoList
                        .stream()
                        .filter(info -> info.getMediaId().equals(mediaId))
                        .findFirst()
                        .orElse(null);
                if (songInfo != null) {
                    OptionalInt position = IntStream.range(0, songInfoList.size())
                            .filter(i -> songInfo.getMediaId().equals(songInfoList.get(i).getMediaId()))
                            .findFirst();
                    Log.i(TAG, "onSongChange indexOpt: " + position);
                    int songPosition = position.isPresent() ? position.getAsInt() : -1;
                    mMediaMetaData.setCurrentSongPosition(songPosition);
                    mMediaMetaData.setCurrentSong(songInfo);
                    if(mSourceManager.getActiveSource().getType() == MediaInterfaceConstant.SourceType.USB1 ||
                            mSourceManager.getActiveSource().getType() == MediaInterfaceConstant.SourceType.USB2){
                        Log.i(TAG, "Fetching cover art for song: " + mMediaMetaData.getCurrentSong().getDisplayName());
                        Log.i(TAG, "isLocal Path " + mMediaMetaData.getCurrentSong().isLocalPath());
                        if(mMediaMetaData.getCurrentSong().isLocalPath()){
                            mMediaMetaData.getCurrentSong().setCovertArtImage(Utils.getCoverArtLocal(mContext, Uri.parse(mMediaMetaData.getCurrentSong().getCoverArtUrl())));
                        } else {
                            mMediaMetaData.getCurrentSong().setCovertArtImage(Utils.getUSBCoverArtImage(mContext,mMediaMetaData.getCurrentSong().getCoverArtUrl()));
                        }
                    }
                    int n = mCallbackList.beginBroadcast();
                    Log.i(TAG, "onSongChange callback count: " + n);
                    try {
                        for (int i = 0; i < n; i++) {
                            Log.i(TAG, ">onSongChange: " + i);
                            mCallbackList.getBroadcastItem(i).onSongChange(songInfo, songPosition);
                            Log.i(TAG, "<onSongChange: " + i);
                        }
                    } catch (RemoteException ex) {
                        Log.i(TAG, "onSongChange: " + ex.getMessage());
                    } finally {
                        mCallbackList.finishBroadcast();
                    }
                    updateWidget(songInfo.getTitle());
                }
            }
        }
    }

    @Override
    public void onPlaybackStarted(boolean isStarted) {
        synchronized (mCallbackList) {
            mMediaMetaData.setIsPlaying(isStarted);
            final int n = mCallbackList.beginBroadcast();
            Log.i(TAG, "onPlaybackStarted callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">onPlaybackStarted: " + i);
                    mCallbackList.getBroadcastItem(i).onPlaybackStarted(isStarted);
                    Log.i(TAG, "<onPlaybackStarted: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, "onPlaybackStarted: " + ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }

    @Override
    public void onShuffleModeChange(boolean isShuffleEnabled) {
        synchronized (mCallbackList) {
            mMediaMetaData.setIsShuffleEnabled(isShuffleEnabled);
            final int n = mCallbackList.beginBroadcast();
            Log.i(TAG, "onShuffleModeChange callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">onShuffleModeChange: " + i);
                    mCallbackList.getBroadcastItem(i).onShuffleModeChange(isShuffleEnabled);
                    Log.i(TAG, "<onShuffleModeChange: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            synchronized (mAlexaMediaCallbackList) {
                final int n = mAlexaMediaCallbackList.beginBroadcast();
                Log.i(TAG, "onPlayerStateChanged callback count: " + n);
                try {
                    for (int i = 0; i < n; i++) {
                        Log.i(TAG, ">onPlayerStateChanged: " + i);
                        mAlexaMediaCallbackList.getBroadcastItem(i).onPlayerStateChanged(playWhenReady, playbackState);
                        Log.i(TAG, "<onPlayerStateChanged: " + i);
                    }
                } catch (RemoteException ex) {
                    Log.i(TAG, ex.getMessage());
                } finally {
                    mAlexaMediaCallbackList.finishBroadcast();
                }
            }
    }

    @Override
    public void onProgressToAlexa(long position, long duration) {
    if (mMediaMetaData != null && mMediaMetaData.getCurrentSong().getIsAlexaSource()){
            synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "onProgressToAlexa callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">onPlayerStateChanged: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).onProgressToAlexa(position, duration);
                    Log.i(TAG, "<onProgressToAlexa: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }
    } else {
        Log.i(TAG, "MetaData is null or Source is not Alexa");
    }
    }

    @Override
    public void processMediaError(String message) {
        synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "processMediaError callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">processMediaError: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).processMediaError(message);
                    Log.i(TAG, "<processMediaError: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }
    }

    @Override
    public void onPlayerError(String errorMsg) {
        synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "onPlayerError callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">onPlayerError: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).onPlayerError(errorMsg);
                    Log.i(TAG, "<onPlayerError: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }
    }

    public void register(IMediaServiceCallback mediaServiceCallback) {
        mCallbackList.register(mediaServiceCallback);
    }

    public void unregister(IMediaServiceCallback mediaServiceCallback) {
        mCallbackList.unregister(mediaServiceCallback);
    }

    public void registerAlexaCallback(IAlexaMediaServiceCallback alexaServiceCallback) {
        mAlexaMediaCallbackList.register(alexaServiceCallback);
    }

    public void unregisterAlexaCallback(IAlexaMediaServiceCallback alexaServiceCallback) {
        mAlexaMediaCallbackList.unregister(alexaServiceCallback);
    }

    private void registerReceiver() {
        Log.i(TAG, "registerReceiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaInterfaceConstant.WIDGET_ACTION_REGISTER);
        intentFilter.addAction(MediaInterfaceConstant.WIDGET_ACTION_UNREGISTER);
        intentFilter.addAction(MediaInterfaceConstant.WIDGET_ACTION_PREVIOUS);
        intentFilter.addAction(MediaInterfaceConstant.WIDGET_ACTION_NEXT);
        intentFilter.addAction(MediaInterfaceConstant.WIDGET_ACTION_GET_SONG);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "widgetReceiver onReceive" + intent);
                if (intent != null) {
                    Log.i(TAG, "mAppWidgetId: " + Arrays.toString(mAppWidgetId.toArray()));
                    if (MediaInterfaceConstant.WIDGET_ACTION_REGISTER.equals(intent.getAction())) {
                        mAppWidgetId.add(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
                        getCurrentSong();
                    } else if (MediaInterfaceConstant.WIDGET_ACTION_UNREGISTER.equals(intent.getAction())) {
                        mAppWidgetId.remove(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
                    } else if (MediaInterfaceConstant.WIDGET_ACTION_GET_SONG.equals(intent.getAction())) {
                        getCurrentSong();
                    } else if (MediaInterfaceConstant.WIDGET_ACTION_PREVIOUS.equals(intent.getAction())) {
                        onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PREVIOUS);
                    } else if (MediaInterfaceConstant.WIDGET_ACTION_NEXT.equals(intent.getAction())) {
                        onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_NEXT);
                    } else {
                        Log.w(TAG, "onReceive action is not correct");
                    }
                } else {
                    Log.w(TAG, "onReceive intent is null");
                }
            }
        }, intentFilter);

        Intent intent = new Intent(MediaInterfaceConstant.WIDGET_ACTION_REGISTER);
        intent.setPackage(MediaInterfaceConstant.PACKAGE_MEDIA);
        mContext.sendBroadcast(intent);
        Log.i(TAG, "registerReceiver " + intent);
    }

    private void getCurrentSong() {
        if (mMediaMetaData != null) {
            SongInfo songInfo = mMediaMetaData.getCurrentSong();
            if (songInfo != null) {
                updateWidget(songInfo.getTitle());
            } else {
                Log.e(TAG, "songInfo is null");
            }
        } else {
            Log.e(TAG, "mMediaMetaData is null");
        }
    }

    private void updateWidget(String title) {
        for (int widgetId : mAppWidgetId) {
            Intent intent = new Intent(MediaInterfaceConstant.WIDGET_ACTION_SONG_CHANGE);
            intent.setPackage(MediaInterfaceConstant.PACKAGE_MEDIA);
            intent.putExtra(MediaInterfaceConstant.MEDIA_TITLE, title);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void onSourceStateChange(MediaInterfaceConstant.SourceType sourceType, MediaInterfaceConstant.SourceState sourceState) {
        Log.d(TAG, "onSourceStateChange: " + sourceType + " " + sourceState);
        if (mSourceManager.getActiveSource() != null) {
            if (mSourceManager.getActiveSource().getType().equals(sourceType)) {

                if (mSourceManager.getActiveSource().getType().equals(MediaInterfaceConstant.SourceType.ALEXA)) {

                    updateSourceChange(sourceType);

                } else if (sourceState.equals(MediaInterfaceConstant.SourceState.CONNECTED)) {
                    mSourceManager.setPreviousSourceState(sourceState);
                    updateSourceChange(sourceType);
                } else if (sourceState.equals(MediaInterfaceConstant.SourceState.READY)) {
                    if (mSourceManager.getPreviousSourceState().equals(MediaInterfaceConstant.SourceState.CONNECTED)) {
                        new Handler(mContext.getMainLooper()).post(() -> {
                            List<SongInfo> songInfoList = mSourceManager.getActiveSource().getSongs();
                            mMusicPlayer.updateMediaSource(songInfoList);
                            updateMediaMetaData();
                        });
                    } else {
                        updateSourceChange(sourceType);
                    }
                    mSourceManager.setPreviousSourceState(sourceState);
                }
            } else {
                getAllSource();
            }
        } else {
            Log.e(TAG, "Active source is null");
        }
    }

    public void changeSource(MediaInterfaceConstant.SourceType sourceType, boolean isFromAlexa) {
        if (mSourceManager.getActiveSource() != null) {
            sendBroadcastToLaunchMediaApp();
            sendSourceChangeCallbackToAlexa(sourceType, isFromAlexa);
            if (mSourceManager.getActiveSource().getType().equals(MediaInterfaceConstant.SourceType.ALEXA)
                    && !sourceType.equals(MediaInterfaceConstant.SourceType.ALEXA)) {
                unregisterCallback(isFromAlexa);
                mSourceManager.getActiveSource().setSourceState(MediaInterfaceConstant.SourceState.DISCONNECTED);
            }
            if (!mSourceManager.getActiveSource().getType().equals(sourceType)) {
                onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PAUSE);
                playWhenReady = true;
                mSourceManager.changeSource(sourceType);
            } else {
                Log.e(TAG, "Current active source is same: " + sourceType);
            }
        } else {
            Log.e(TAG, "Active source is " + mSourceManager.getActiveSource()
                    + " so switching to the source: " + sourceType);
            playWhenReady = true;
            mSourceManager.changeSource(sourceType);
        }
    }

    public void getAllSource() {
        Log.d(TAG, "getAllSource");
        List<SourceInfo> sourceList = mSourceManager.getAllSource();
        synchronized (mCallbackList) {
            final int n = mCallbackList.beginBroadcast();
            Log.d(TAG, "getAllSource callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.d(TAG, ">getAllSource: " + i);
                    mCallbackList.getBroadcastItem(i).updateSourceInfo(sourceList);
                    Log.d(TAG, "<getAllSource: " + i);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }

    private void updateFavorite() {
        if (mMediaMetaData != null) {
            SongInfo currentSongInfo = mMediaMetaData.getCurrentSong();
            if (currentSongInfo != null) {
                currentSongInfo.setIsFavorite(!currentSongInfo.isFavorite());

                synchronized (mCallbackList) {
                    final int n = mCallbackList.beginBroadcast();
                    Log.d(TAG, "updateFavorite callback count: " + n);
                    try {
                        for (int i = 0; i < n; i++) {
                            Log.d(TAG, ">updateFavorite: " + i);
                            mCallbackList.getBroadcastItem(i).onFavoriteChange(currentSongInfo.isFavorite());
                            Log.d(TAG, "<updateFavorite: " + i);
                        }
                    } catch (RemoteException ex) {
                        Log.e(TAG, ex.getMessage());
                    } finally {
                        mCallbackList.finishBroadcast();
                    }
                }
            }
        }
    }

    private void updateSourceChange(MediaInterfaceConstant.SourceType sourceType) {
        if (!sourceType.equals(MediaInterfaceConstant.SourceType.ALEXA)) {
            new Handler(mContext.getMainLooper()).post(() -> {
                preparePlayer(mSourceManager.getActiveSource().getSongs());
                updateMediaMetaData();
            });
        } else {
            Log.i(TAG, " Source is " + sourceType);
        }
        synchronized (mCallbackList) {
            final int n = mCallbackList.beginBroadcast();
            Log.d(TAG, "updateSourceChange callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.d(TAG, ">updateSourceChange: " + i);
                    mCallbackList.getBroadcastItem(i).onSourceChange(sourceType.toString());
                    Log.d(TAG, "<updateSourceChange: " + i);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }

    private void sendSourceChangeCallbackToAlexa(MediaInterfaceConstant.SourceType sourceType, boolean isFromAlexa) {
        synchronized (mAlexaMediaCallbackList) {
            final int n = mAlexaMediaCallbackList.beginBroadcast();
            Log.i(TAG, "sendSourceChangeCallbackToAlexa callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">sendSourceChangeCallbackToAlexa: " + i);
                    mAlexaMediaCallbackList.getBroadcastItem(i).onSourceChanged(sourceType.name(), isFromAlexa);
                    Log.i(TAG, "<sendSourceChangeCallbackToAlexa: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, ex.getMessage());
            } finally {
                mAlexaMediaCallbackList.finishBroadcast();
            }
        }
    }

    public List<MediaSettings> getMediaSettings() {
        return mSettingsManager.getMediaSettings();
    }

    public void updateSettings(String settingsName, boolean settingsValue) {
        mSettingsManager.updateSettings(settingsName, settingsValue);

        synchronized (mCallbackList) {
            final int n = mCallbackList.beginBroadcast();
            Log.d(TAG, "updateSettings callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.d(TAG, ">updateSettings: " + i);
                    mCallbackList.getBroadcastItem(i).onSettingsChange(mSettingsManager.getMediaSettings());
                    Log.d(TAG, "<updateSettings: " + i);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                mCallbackList.finishBroadcast();
            }
        }
    }
    public MediaEqualizerPreset getMediaEqualizerPreset(){
        return mEqualizerManager.getEqualizerProperties();
    }

    public void updateEqualizerPreset() {
        if (mMediaEqualizerPreset != null) {
            synchronized (mCallbackList) {
                int n = mCallbackList.beginBroadcast();
                Log.i(TAG, "getMediaEqualizerPreset callback: " + n);
                try {
                    for (int i = 0; i < n; i++) {
                        Log.i(TAG, ">getEqualizerPreset: " + i);
                        mCallbackList.getBroadcastItem(i).onPresetChange(mMediaEqualizerPreset);
                        Log.i(TAG, "<getEqualizerPreset: " + i);
                    }
                } catch (RemoteException ex) {
                    Log.i(TAG, "updateMediaEqualizerPreset: " + ex.getMessage());
                } finally {
                    mCallbackList.finishBroadcast();
                }
            }
        } else {
            Log.e(TAG, "getMediaEqualizerPreset mMediaEqualizerPreset is null");
        }
    }

    public void prepare(Uri uri) {
        Log.i(TAG, " uri " + uri + " active source " + mSourceManager.getActiveSource().getType());
        if (!mSourceManager.getActiveSource().getType().equals(MediaInterfaceConstant.SourceType.ALEXA)) {
            changeSource(MediaInterfaceConstant.SourceType.ALEXA, true);
        }
        sendBroadcastToLaunchMediaApp();

        SongInfo songInfo = new SongInfo();
        songInfo.setSongUri(uri);
        mSourceManager.getActiveSource().setSongInfo(songInfo);

        new Handler(mContext.getMainLooper()).post(() -> {
            mMusicPlayer.prepare(uri);
        });
    }

    private void sendBroadcastToLaunchMediaApp() {
        Intent intent = new Intent("android.appwidget.action.LAUNCH_MEDIA_MINI_ACTIVITY");
        intent.setPackage("com.quest.macaw.media");
        mContext.sendBroadcast(intent);
    }

    public void requestPlay() {
        new Handler(mContext.getMainLooper()).post(mMusicPlayer::play);
    }

    public void requestPause() {
        new Handler(mContext.getMainLooper()).post(mMusicPlayer::pause);

    }

    public void requestStop() {
        new Handler(mContext.getMainLooper()).post(mMusicPlayer::stop);

    }

    public void setVolume(float multiplier) {
        new Handler(mContext.getMainLooper()).post(() -> {
            mMusicPlayer.setVolume(multiplier);
        });

    }

    public void seekToPosition(long position) {
        new Handler(mContext.getMainLooper()).post(() -> {
            mMusicPlayer.seekTo(position);
        });

    }

    public void setSongInfo(SongInfo songInfo) {
        boolean isPlaying = false;
        if (mMediaMetaData != null) {
            isPlaying = mMediaMetaData.isIsPlaying();
            if(mMediaMetaData.getCurrentSong()!=null && mMediaMetaData.getCurrentSong().getTitle().equals(songInfo.getTitle())){
             Log.i(TAG, " Same song as previous :" + songInfo.getTitle());
                return;
            }
        }
        mMediaMetaData = new MediaMetaData();
        if (mSourceManager.getActiveSource().getType().equals(MediaInterfaceConstant.SourceType.ALEXA)) {
              mMediaMetaData = new MediaMetaData();
            if (mSourceManager.getActiveSource().getSongs() != null) {
                SongInfo activeSongInfo = mSourceManager.getActiveSource().getSongs().get(0);
                activeSongInfo.setIsAlexaSource(true);
                activeSongInfo.setTitle(songInfo.getTitle());
                activeSongInfo.setAlbumName(songInfo.getAlbumName());
                activeSongInfo.setArtistName(songInfo.getArtistName());
                activeSongInfo.setCoverArtUrl(songInfo.getCoverArtUrl());
                activeSongInfo.setDuration(songInfo.getDuration());
                activeSongInfo.setProviderLogoUrl(songInfo.getProviderLogoUrl());
                mSourceManager.getActiveSource().setSongInfo(activeSongInfo);
                mMediaMetaData.setIsPlaying(isPlaying);
                mMediaMetaData.setSongInfoList(mSourceManager.getActiveSource().getSongs());
                mMediaMetaData.setCurrentSong(mSourceManager.getActiveSource().getSongs().get(0));
                Log.i(TAG, " alexa active song info " + activeSongInfo.toString());
                    new QueryExecutor(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                InputStream input = new java.net.URL(songInfo.getCoverArtUrl()).openStream();
                                // Decode Bitmap
                                activeSongInfo.setCovertArtImage(BitmapFactory.decodeStream(input));
                                updateMediaMetaData();
                                updateWidget(activeSongInfo.getTitle());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).execute();

            }
        } else {
            Log.i(TAG, " active source is not alexa");
        }
    }

    public void markFavorite(boolean isFavourite) {
        if (mMediaMetaData != null) {
            SongInfo currentSongInfo = mMediaMetaData.getCurrentSong();
            currentSongInfo.setIsFavorite(!isFavourite);
            updateFavorite();
        } else {
            Log.w(TAG, " mMediaMetaData is null");
        }
    }

    public void shuffle(boolean isEnabled) {
        if (mMusicPlayer != null) {
            new Handler(mContext.getMainLooper()).post(() -> {
                mMusicPlayer.shuffle(isEnabled);
            });
        } else {
            Log.w(TAG, " mMusicPlayer is null");
        }
    }

    public void repeat(boolean isEnabled) {
        if (mMusicPlayer != null) {
            new Handler(mContext.getMainLooper()).post(() -> {
                mMusicPlayer.loop(isEnabled);
            });
        } else {
            Log.w(TAG, " mMusicPlayer is null");
        }
    }

    public void startOver() {
        if (mMusicPlayer != null) {
            new Handler(mContext.getMainLooper()).post(mMusicPlayer::startOver);
        } else {
            Log.w(TAG, " mMusicPlayer is null");
        }
    }
}
