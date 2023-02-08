// IMediaServiceInterface.aidl
package com.quest.macaw.media.common;

// Declare any non-default types here with import statements
import com.quest.macaw.media.common.IMediaServiceCallback;
import com.quest.macaw.media.common.IAlexaMediaServiceInterface;
import com.quest.macaw.media.common.MediaMetaData;
import com.quest.macaw.media.common.MediaSettings;
import com.quest.macaw.media.common.MediaEqualizerSettings;
import com.quest.macaw.media.common.MediaEqualizerPreset;

interface IMediaServiceInterface {

    void registerCallback(IMediaServiceCallback mediaServiceCallback);

    void unregisterCallback(IMediaServiceCallback mediaServiceCallback);

    MediaMetaData getMetaData();

    void onControlAction(int controlAction);

    void loadMusic(String mediaId);

    void seekTo(int progress);

    void changeSource(String sourceType);

    void getAllSource();

    List<MediaSettings> getMediaSettings();

    MediaEqualizerPreset getEqualizerPreset();

    void updateSettings(String settingsName, boolean settingsValue);

    void insertData(in MediaEqualizerSettings mediaEqualizerSettings);

    void updateData(in MediaEqualizerSettings mediaEqualizerSettings);

    void updateEqualizerBands(int band,int level);

    void updateSelectedPreset(int preset);

    void updatePresetName();

    void getAllEqualizerData();

    void updateFirstBand(int firstBand);

    void updateSecondBand(int secondBand);

    void updateThirdBand(int thirdBand);

    void updateFourthBand(int fourthBand);

    void updateFifthBand(int fifthBand);

    void updateAllEqualizerBands(in int[] equalizerBands);

    IAlexaMediaServiceInterface getAlexaServiceInterface();
}
