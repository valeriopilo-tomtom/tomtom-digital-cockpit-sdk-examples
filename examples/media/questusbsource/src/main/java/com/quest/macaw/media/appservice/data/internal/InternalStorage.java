package com.quest.macaw.media.appservice.data.internal;

import android.content.Context;

import com.quest.macaw.media.appservice.data.interfaces.OnSourceStateChangeListener;
import com.quest.macaw.media.appservice.data.interfaces.Sources;
import com.quest.macaw.media.appservice.data.store.MediaScanner;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class InternalStorage implements Sources {

    private static final MediaInterfaceConstant.SourceType mSourceType = MediaInterfaceConstant.SourceType.INTERNAL_MEMORY;
    private final OnSourceStateChangeListener mOnSourceStateChangeListener;
    private List<SongInfo> mSongInfoList = new ArrayList<>();
    private MediaInterfaceConstant.SourceState mSourceState;

    public InternalStorage(Context context, OnSourceStateChangeListener stateChangeListener) {
        mOnSourceStateChangeListener = stateChangeListener;
        mSourceState = MediaInterfaceConstant.SourceState.INITIALIZING;
        MediaScanner.scanFile(context, MediaInterfaceConstant.INTERNAL_STORAGE_PATH, new MediaScanner.OnMediaScannerComplete() {
            @Override
            public void onScan(List<SongInfo> songInfoList) {
                mSourceState = MediaInterfaceConstant.SourceState.CONNECTED;
                mSongInfoList = songInfoList;
                if (stateChangeListener != null) {
                    stateChangeListener.onSourceStateChange(MediaInterfaceConstant.SourceType.INTERNAL_MEMORY, mSourceState);
                }
            }

            @Override
            public void onScanComplete(List<SongInfo> songInfoList) {
                mSourceState = MediaInterfaceConstant.SourceState.READY;
                mSongInfoList = songInfoList;
                if (stateChangeListener != null) {
                    stateChangeListener.onSourceStateChange(MediaInterfaceConstant.SourceType.INTERNAL_MEMORY, mSourceState);
                }
            }
        });
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
    public String getPath() {
        return MediaInterfaceConstant.INTERNAL_STORAGE_PATH;
    }

    @Override
    public List<SongInfo> getSongs() {
        return mSongInfoList;
    }

    @Override
    public void changeSource() {
        if (mOnSourceStateChangeListener != null) {
            mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
        }
    }
}
