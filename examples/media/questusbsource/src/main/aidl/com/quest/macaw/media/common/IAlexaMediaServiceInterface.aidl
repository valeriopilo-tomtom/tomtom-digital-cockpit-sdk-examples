// IAlexaMediaServiceInterface.aidl
package com.quest.macaw.media.common;

// Declare any non-default types here with import statements
import com.quest.macaw.media.common.IAlexaMediaServiceCallback;
import com.quest.macaw.media.common.SongInfo;

interface IAlexaMediaServiceInterface {

    void registerCallback(IAlexaMediaServiceCallback alexaMediaServiceCallback);

    void unregisterCallback(IAlexaMediaServiceCallback alexaMediaServiceCallback);

    void prepare(in Uri uri);

    void requestPlay();

    void requestPause();

    void requestStop();

    void setVolume(float multiplier);

    void seekToPosition(long position);

    void setSongInfo(in SongInfo songInfo);

    void changeSource(String sourceType);

    void playNextSong();

    void playPreviousSong();

    void markFavorite(boolean isFavourite);

    void shuffle(boolean isEnabled);

    void repeat(boolean isEnabled);

    void startOver();
}