package com.quest.macaw.media.appservice.data;

import android.content.Context;
import android.util.Log;

import com.quest.macaw.media.appservice.data.alexa.AlexaImpl;
import com.quest.macaw.media.appservice.data.interfaces.OnSourceStateChangeListener;
import com.quest.macaw.media.appservice.data.interfaces.Sources;
import com.quest.macaw.media.appservice.data.internal.InternalStorage;
import com.quest.macaw.media.appservice.data.usb.USBManagerImpl;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SourceInfo;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SourceManager {

    private static final String TAG = "SourceManager";

    private static final Object mLock = new Object();
    private static SourceManager mSourceManager;
    private final Context mContext;
    private MediaInterfaceConstant.SourceState mPreviousSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
    ;


    private final EnumMap<MediaInterfaceConstant.SourceType, Sources> mSources = new EnumMap<>(MediaInterfaceConstant.SourceType.class);
    private MediaInterfaceConstant.SourceType mActiveSource;

    private SourceManager(Context context, OnSourceStateChangeListener stateChangeListener) {
        mContext = context;
        mSources.put(MediaInterfaceConstant.SourceType.INTERNAL_MEMORY, new InternalStorage(context, stateChangeListener));
        mSources.put(MediaInterfaceConstant.SourceType.USB1, new USBManagerImpl(context, stateChangeListener));
        mSources.put(MediaInterfaceConstant.SourceType.ALEXA, new AlexaImpl(context, stateChangeListener));
        mActiveSource = MediaInterfaceConstant.SourceType.INTERNAL_MEMORY;
    }

    public static SourceManager getInstance(Context context, OnSourceStateChangeListener stateChangeListener) {
        synchronized (mLock) {
            if (mSourceManager == null) {
                mSourceManager = new SourceManager(context, stateChangeListener);
            }
            return mSourceManager;
        }
    }

    public Map<MediaInterfaceConstant.SourceType, Sources> getAllSources() {
        return mSources;
    }

    public Sources getSource(MediaInterfaceConstant.SourceType sourceTypes) {
        return mSources.get(sourceTypes);
    }

    public void addSource(MediaInterfaceConstant.SourceType sourceTypes, Sources sources) {
        mSources.put(sourceTypes, sources);
    }

    public void removeSource(MediaInterfaceConstant.SourceType sourceTypes) {
        mSources.remove(sourceTypes);
    }

    public Sources getActiveSource() {
        return mSources.get(mActiveSource);
    }

    public void setActiveSource(MediaInterfaceConstant.SourceType activeSource) {
        mActiveSource = activeSource;
    }

    public MediaInterfaceConstant.SourceState getPreviousSourceState() {
        return mPreviousSourceState;
    }

    public void setPreviousSourceState(MediaInterfaceConstant.SourceState previousSourceState) {
        this.mPreviousSourceState = previousSourceState;
    }

    public void changeSource(MediaInterfaceConstant.SourceType sourceType) {
        Log.d(TAG, "changeSource: " + sourceType);
        if (!mActiveSource.equals(sourceType)) {
            mActiveSource = sourceType;
            Sources source = mSources.get(sourceType);
            if (source != null) {
                mPreviousSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
                source.changeSource();
            } else {
                Log.e(TAG, "Source type is not available: " + sourceType);
            }
        } else {
            Log.w(TAG, "Selected and active sources are same: " + mActiveSource);
        }
    }

    public List<SourceInfo> getAllSource() {
        Log.d(TAG, "getAllSource: ");
        Set<MediaInterfaceConstant.SourceType> sourceTypes = mSources.keySet();
        List<SourceInfo> sourceList = new ArrayList<>();
        for (MediaInterfaceConstant.SourceType type : sourceTypes) {
            Sources source = mSources.get(type);
            if (source != null) {
                SourceInfo sourceInfo = new SourceInfo();
                sourceInfo.setSource(type.toString());
                sourceInfo.setStatus(source.getState().toString());
                if (source.getType().equals(mActiveSource)) {
                    sourceInfo.setActive(true);
                } else {
                    sourceInfo.setActive(false);
                }
                sourceList.add(sourceInfo);
            } else {
                Log.w(TAG, "Source is null: " + type);
            }
        }
        SourceInfo sourceInfoBt = new SourceInfo();
        sourceInfoBt.setSource(MediaInterfaceConstant.SourceType.BLUETOOTH.toString());
        sourceInfoBt.setStatus(MediaInterfaceConstant.SourceState.DISCONNECTED.toString());
        sourceInfoBt.setActive(false);
        sourceList.add(sourceInfoBt);
        SourceInfo sourceInfoAm = new SourceInfo();
        sourceInfoAm.setSource(MediaInterfaceConstant.SourceType.AM.toString());
        sourceInfoAm.setStatus(MediaInterfaceConstant.SourceState.DISCONNECTED.toString());
        sourceInfoAm.setActive(false);
        sourceList.add(sourceInfoAm);
        SourceInfo sourceInfoIpod = new SourceInfo();
        sourceInfoIpod.setSource(MediaInterfaceConstant.SourceType.IPOD.toString());
        sourceInfoIpod.setStatus(MediaInterfaceConstant.SourceState.DISCONNECTED.toString());
        sourceInfoIpod.setActive(false);
        sourceList.add(sourceInfoIpod);

        Log.d(TAG, "getAllSource size: " + sourceList.size());

        return sourceList;
    }
}
