package com.quest.macaw.media.appservice.data;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.quest.macaw.media.common.IAlexaMediaServiceCallback;
import com.quest.macaw.media.common.IAlexaMediaServiceInterface;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SongInfo;

public class AlexaServiceInterface extends IAlexaMediaServiceInterface.Stub{

    private static final String TAG = AlexaServiceInterface.class.getSimpleName();
    private final Context mContext;
    private final MediaServiceManager mMediaServiceManager;

    public AlexaServiceInterface(Context context, MediaServiceManager mediaServiceManager) {
        Log.d(TAG, " AlexaServiceInterface initialized");
        mContext = context;
        mMediaServiceManager = mediaServiceManager;
    }

    @Override
    public void registerCallback(IAlexaMediaServiceCallback alexaServiceCallback) throws RemoteException {
        Log.d(TAG, " registerCallback alexa");
        mMediaServiceManager.registerAlexaCallback(alexaServiceCallback);
    }

    @Override
    public void unregisterCallback(IAlexaMediaServiceCallback alexaServiceCallback) throws RemoteException {
        Log.d(TAG, " unregisterCallback alexa");
        mMediaServiceManager.unregisterAlexaCallback(alexaServiceCallback);
    }

    @Override
    public void prepare(Uri uri) throws RemoteException {
      mMediaServiceManager.prepare(uri);
    }

    @Override
    public void requestPlay() throws RemoteException {
       mMediaServiceManager.requestPlay();
    }

    @Override
    public void requestPause() throws RemoteException {
      mMediaServiceManager.requestPause();
    }

    @Override
    public void requestStop() throws RemoteException {
          mMediaServiceManager.requestStop();
    }

    @Override
    public void setVolume(float multiplier) throws RemoteException {
         mMediaServiceManager.setVolume(multiplier);
    }

    @Override
    public void seekToPosition(long position) throws RemoteException {
        mMediaServiceManager.seekToPosition(position);
    }

    @Override
    public void setSongInfo(SongInfo songInfo) throws RemoteException {
        mMediaServiceManager.setSongInfo(songInfo);
    }

    @Override
    public void changeSource(String sourceType) throws RemoteException {
        mMediaServiceManager.changeSource(MediaInterfaceConstant.SourceType.valueOf(sourceType), true);
    }

    @Override
    public void playNextSong() throws RemoteException {
        mMediaServiceManager.onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_NEXT);
    }

    @Override
    public void playPreviousSong() throws RemoteException {
        mMediaServiceManager.onControlAction(MediaInterfaceConstant.MEDIA_CONTROL_ACTION_PREVIOUS);
    }

    @Override
    public void markFavorite(boolean isFavourite) throws RemoteException {
        mMediaServiceManager.markFavorite(isFavourite);
    }

    @Override
    public void shuffle(boolean isEnabled) throws RemoteException {
      mMediaServiceManager.shuffle(isEnabled);
    }

    @Override
    public void repeat(boolean isEnabled) throws RemoteException {
       mMediaServiceManager.repeat(isEnabled);
    }

    @Override
    public void startOver() throws RemoteException {
       mMediaServiceManager.startOver();
    }

}
