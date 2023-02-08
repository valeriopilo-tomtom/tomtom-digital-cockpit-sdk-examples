package com.quest.macaw.media.appservice.data.usb;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.quest.macaw.media.appservice.data.interfaces.OnSourceStateChangeListener;
import com.quest.macaw.media.appservice.data.interfaces.Sources;
import com.quest.macaw.media.appservice.data.interfaces.USBManager;
import com.quest.macaw.media.appservice.data.store.MediaScanner;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SongInfo;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.ArrayList;
import java.util.List;

public class USBManagerImpl implements USBManager, Sources {

    private static final String TAG = "USBManagerImpl";
    private static final MediaInterfaceConstant.SourceType mSourceType = MediaInterfaceConstant.SourceType.USB1;
    private final Context mContext;
    private final OnSourceStateChangeListener mOnSourceStateChangeListener;
    private MediaInterfaceConstant.SourceState mSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
    private List<SongInfo> mSongInfoList = new ArrayList<>();
    private StorageManager mStorageManager;
    private String mSourcePath;

    public USBManagerImpl(Context context, OnSourceStateChangeListener stateChangeListener) {
        mContext = context;
        mOnSourceStateChangeListener = stateChangeListener;
        mStorageManager = mContext.getSystemService(StorageManager.class);
        mStorageManager.registerStorageVolumeCallback(executor, callback);
        for (StorageVolume storageVolume : mStorageManager.getStorageVolumes()) {
            Log.i(TAG, "USBManagerImpl: removable device is already connected");
            if (storageVolume.isRemovable()) {
                File file = storageVolume.getDirectory();
                if (file != null) {
                    MediaScanner.scanFile(mContext, file.getAbsolutePath(), new MediaScanner.OnMediaScannerComplete() {
                        @Override
                        public void onScan(List<SongInfo> songInfoList) {
                            mSongInfoList = songInfoList;
                            mSourceState = MediaInterfaceConstant.SourceState.CONNECTED;
                            if (mOnSourceStateChangeListener != null) {
                                mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
                            }
                        }

                        @Override
                        public void onScanComplete(List<SongInfo> songInfoList) {
                            mSongInfoList = songInfoList;
                            mSourceState = MediaInterfaceConstant.SourceState.READY;
                            if (mOnSourceStateChangeListener != null) {
                                mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "USBManagerImpl: file is null");
                }
            }
        }
    }

    @Override
    public void onMount() {
        if (mContext != null) {

        }
    }

    @Override
    public void onUnMount() {

    }

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private StorageManager.StorageVolumeCallback callback = new StorageManager.StorageVolumeCallback() {
         @Override
         public void onStateChanged(StorageVolume volume) {
            onPublicVolumeStateChangedInternal(volume);
            Log.d(TAG, "onStateChanged " + volume.toString());
         }
    };

/*
    private final StorageEventListener mListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            onPublicVolumeStateChangedInternal(vol);
            Log.d(TAG, "onVolumeStateChanged " + vol.toString());
        }

        @Override
        public void onVolumeRecordChanged(VolumeRecord rec) {
            Log.d(TAG, "onVolumeRecordChanged");
            // Avoid kicking notifications when getting early metadata before
            // mounted. If already mounted, we're being kicked because of a
            // nickname or init'ed change.
            final VolumeInfo vol = mStorageManager.findVolumeByUuid(rec.getFsUuid());
            if (vol != null && vol.isMountedReadable()) {
                onPublicVolumeStateChangedInternal(vol);
            }
        }

        @Override
        public void onVolumeForgotten(String fsUuid) {
            Log.d(TAG, "onVolumeRecordChanged");
            mSourcePath = "";
            mSongInfoList = new ArrayList<>();
            mSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
        }
    };
*/
    private void onPublicVolumeStateChangedInternal(StorageVolume vol) {
        Log.d(TAG, "Root " + vol.getDirectory());

        switch (vol.getState()) {
            case Environment.MEDIA_MOUNTED:
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                Log.d(TAG, "Mounted");
                mSourcePath = vol.getDirectory().getAbsolutePath();
                MediaScanner.scanFile(mContext, mSourcePath, new MediaScanner.OnMediaScannerComplete() {
                        @Override
                        public void onScan(List<SongInfo> songInfoList) {
                            mSongInfoList = songInfoList;
                            mSourceState = MediaInterfaceConstant.SourceState.CONNECTED;
                            if (mOnSourceStateChangeListener != null) {
                                mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
                            }
                        }

                        @Override
                        public void onScanComplete(List<SongInfo> songInfoList) {
                            mSongInfoList = songInfoList;
                            mSourceState = MediaInterfaceConstant.SourceState.READY;
                            if (mOnSourceStateChangeListener != null) {
                                mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
                            }
                        }
                    });
                break;
            case Environment.MEDIA_BAD_REMOVAL:
            case Environment.MEDIA_REMOVED:
            case Environment.MEDIA_UNMOUNTED:
                Log.d(TAG, "Unmounted");
                mSourcePath = "";
                mSongInfoList = new ArrayList<>();
                mSourceState = MediaInterfaceConstant.SourceState.DISCONNECTED;
                if (mOnSourceStateChangeListener != null) {
                    mOnSourceStateChangeListener.onSourceStateChange(mSourceType, mSourceState);
                }
                break;
            default:
                break;
        }
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
        return mSourcePath;
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
