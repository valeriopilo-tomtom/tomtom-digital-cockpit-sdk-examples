package com.quest.macaw.media.appservice.data.alexa;

import android.content.Context;
import android.util.Log;

import com.quest.macaw.media.appservice.data.interfaces.OnSourceStateChangeListener;
import com.quest.macaw.media.appservice.data.interfaces.Sources;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class AlexaImpl implements Sources {

    public static final String TAG = AlexaImpl.class.getSimpleName();
    private static final MediaInterfaceConstant.SourceType mSourceType = MediaInterfaceConstant.SourceType.ALEXA;
    private MediaInterfaceConstant.SourceState mSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
    private final OnSourceStateChangeListener mOnSourceStateChangeListener;
    private List<SongInfo> mSongInfoList ;

    public AlexaImpl(Context context, OnSourceStateChangeListener stateChangeListener) {
        mOnSourceStateChangeListener = stateChangeListener;
    }

    @Override
    public MediaInterfaceConstant.SourceType getType() {
        return mSourceType;
    }

    @Override
    public MediaInterfaceConstant.SourceState getState() {
        return mSourceState;
    }

    @Override
    public void setSourceState(MediaInterfaceConstant.SourceState sourceState) {
        mSourceState = sourceState;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public List<SongInfo> getSongs() {
        return mSongInfoList;
    }

    @Override
    public void changeSource() {
        if (mOnSourceStateChangeListener != null) {
            mSourceState = MediaInterfaceConstant.SourceState.READY;
            Log.i(TAG, " onSourceStateChange " + " mSourceType " + mSourceType + " mSourceState " + mSourceState);
            mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
        } else {
            Log.i(TAG, " mOnSourceStateChangeListener is null");
        }
    }

    @Override
    public void setSongInfo(SongInfo songInfo) {
        mSongInfoList = new ArrayList<>();
        mSongInfoList.add(songInfo);
    }
}
