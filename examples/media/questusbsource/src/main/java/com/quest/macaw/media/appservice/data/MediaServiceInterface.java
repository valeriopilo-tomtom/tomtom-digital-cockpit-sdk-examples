package com.quest.macaw.media.appservice.data;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.quest.macaw.media.common.IAlexaMediaServiceInterface;
import com.quest.macaw.media.common.IMediaServiceCallback;
import com.quest.macaw.media.common.IMediaServiceInterface;
import com.quest.macaw.media.common.MediaEqualizerPreset;
import com.quest.macaw.media.common.MediaEqualizerSettings;
import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.MediaMetaData;
import com.quest.macaw.media.common.MediaSettings;

import java.util.List;

public class MediaServiceInterface extends IMediaServiceInterface.Stub {

    private static final String TAG = "MediaServiceInterface";
    private final MediaServiceManager mMediaServiceManager;
    private MediaEqualizerDatabase mMediaEqualizerDatabase;
    private final EqualizerManager mEqualizerManager;
    private Context mContext;
    private final int EQUALIZER_PRESET_INDEX_CUSTOM = -1;
    private int[] mPreviousCustomEqualizerPresets;
    private int mCurrentlySelectedEQPreset;

    public IMediaServiceInterface.Stub getMediaServiceInterface() {
        return this;
    }

    public MediaServiceInterface(Context context, MediaServiceManager mediaServiceManager, EqualizerManager equalizerManager) {
        mContext = context;
        mMediaServiceManager = mediaServiceManager;
        mMediaEqualizerDatabase = new MediaEqualizerDatabase().getInstance(context);
        mMediaEqualizerDatabase.createDatabase(context);
        this.mEqualizerManager = equalizerManager;
    }

    @Override
    public void registerCallback(IMediaServiceCallback mediaServiceCallback) throws RemoteException {
        Log.i(TAG, "registering callback");
        mMediaServiceManager.register(mediaServiceCallback);
        mMediaEqualizerDatabase.registerCallback(mediaServiceCallback);
    }

    @Override
    public void unregisterCallback(IMediaServiceCallback mediaServiceCallback) throws RemoteException {
        Log.i(TAG, "unregisterCallback");
        mMediaServiceManager.unregister(mediaServiceCallback);
        mMediaEqualizerDatabase.unregisterCallback(mediaServiceCallback);
    }

    @Override
    public MediaMetaData getMetaData() throws RemoteException {
        Log.i(TAG, "getMetaData");
        return mMediaServiceManager.getMediaMetaData();
    }

    @Override
    public void onControlAction(int controlAction) throws RemoteException {
        mMediaServiceManager.onControlAction(controlAction);
    }

    @Override
    public void loadMusic(String mediaId) throws RemoteException {
        mMediaServiceManager.loadMusic(mediaId);
    }

    @Override
    public void seekTo(int progress) throws RemoteException {
        mMediaServiceManager.seekTo(progress);
    }

    @Override
    public void changeSource(String sourceType) throws RemoteException {
        mMediaServiceManager.changeSource(MediaInterfaceConstant.SourceType.valueOf(sourceType), false);
    }

    @Override
    public void getAllSource() throws RemoteException {
        mMediaServiceManager.getAllSource();
    }

    @Override
    public List<MediaSettings> getMediaSettings() throws RemoteException {
        return mMediaServiceManager.getMediaSettings();
    }

    @Override
    public MediaEqualizerPreset getEqualizerPreset() throws RemoteException {
        return mMediaServiceManager.getMediaEqualizerPreset();
    }

    @Override
    public void updateSettings(String settingsName, boolean settingsValue) throws RemoteException {
        mMediaServiceManager.updateSettings(settingsName, settingsValue);
    }

    @Override
    public void insertData(MediaEqualizerSettings mediaEqualizerSettings) {
        mMediaEqualizerDatabase.insertAllData(mediaEqualizerSettings);
    }

    @Override
    public void updateData(MediaEqualizerSettings mediaEqualizerSettings) {
        // mMediaEqualizerDatabase.updateAllData(mediaEqualizerSettings);
    }

    @Override
    public void updateEqualizerBands(int band, int level) throws RemoteException {
        mEqualizerManager.setBandLevels(band, level);
        updateBand(band, level);
        //updateSelectedPreset(-1);
    }

    private void updateBand(int band, int level) {
        switch (band) {
            case 0:
                mMediaEqualizerDatabase.updateFirstBand(level);
                Log.i(TAG, "database update first band: " + level);
                break;
            case 1:
                mMediaEqualizerDatabase.updateSecondBand(level);
                Log.i(TAG, "database update second band: " + level);
                break;
            case 2:
                mMediaEqualizerDatabase.updateThirdBand(level);
                Log.i(TAG, "database update third band: " + level);
                break;
            case 3:
                mMediaEqualizerDatabase.updateFourthBand(level);
                Log.i(TAG, "database update fourth band: " + level);
                break;
            case 4:
                mMediaEqualizerDatabase.updateFifthBand(level);
                Log.i(TAG, "database update fifth band: " + level);
                break;
        }
    }

    private void updatePresetIndex(int preset) {
        mMediaEqualizerDatabase.updatePresetIndex(preset);
    }

    @Override
    public void updateSelectedPreset(int preset) throws RemoteException {
        mCurrentlySelectedEQPreset = preset;
        mEqualizerManager.setSelectedPreset(preset);
        if(preset == EQUALIZER_PRESET_INDEX_CUSTOM){
            updateEQValues(mPreviousCustomEqualizerPresets);
        }
        updatePresetIndex(preset);
        Log.e("updateSelectedPreset", "testing update selected preset " + preset);
    }

    @Override
    public void updatePresetName() throws RemoteException {
        mMediaServiceManager.updateEqualizerPreset();
    }

    @Override
    public void getAllEqualizerData() throws RemoteException {
        mMediaEqualizerDatabase.getCustomBandLevels();
    }

    @Override
    public void updateFirstBand(int firstBand) throws RemoteException {
        mMediaEqualizerDatabase.updateFirstBand(firstBand);
    }

    @Override
    public void updateSecondBand(int secondBand) throws RemoteException {
        mMediaEqualizerDatabase.updateSecondBand(secondBand);
    }

    @Override
    public void updateThirdBand(int thirdBand) throws RemoteException {
        mMediaEqualizerDatabase.updateThirdBand(thirdBand);
    }

    @Override
    public void updateFourthBand(int fourthBand) throws RemoteException {
        mMediaEqualizerDatabase.updateFourthBand(fourthBand);
    }

    @Override
    public void updateFifthBand(int fifthBand) throws RemoteException {
        mMediaEqualizerDatabase.updateFifthBand(fifthBand);
    }

    @Override
    public void updateAllEqualizerBands(int[] equalizerBands) throws RemoteException {
        if(mCurrentlySelectedEQPreset == EQUALIZER_PRESET_INDEX_CUSTOM){
            mPreviousCustomEqualizerPresets = equalizerBands;
        }
        updateEQValues(equalizerBands);
        mMediaEqualizerDatabase.updateAllBandsLevels(equalizerBands);
    }
    @Override
    public IAlexaMediaServiceInterface getAlexaServiceInterface() throws RemoteException {
        return new AlexaServiceInterface(mContext, mMediaServiceManager);
    }

    private void updateEQValues(int[] equalizerBands){
        if(equalizerBands != null){
            mEqualizerManager.setBandLevels(1,equalizerBands[0]);
            mEqualizerManager.setBandLevels(2,equalizerBands[1]);
            mEqualizerManager.setBandLevels(3,equalizerBands[2]);
            mEqualizerManager.setBandLevels(4,equalizerBands[3]);
            mEqualizerManager.setBandLevels(5,equalizerBands[4]);
        }
    }
}
