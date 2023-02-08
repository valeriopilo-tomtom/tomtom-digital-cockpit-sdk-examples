// IMediaServiceCallback.aidl
package com.quest.macaw.media.common;

// Declare any non-default types here with import statements
import com.quest.macaw.media.common.MediaMetaData;
import com.quest.macaw.media.common.MediaSettings;
import com.quest.macaw.media.common.SongInfo;
import com.quest.macaw.media.common.SourceInfo;
import com.quest.macaw.media.common.MediaEqualizerSettings;
import com.quest.macaw.media.common.MediaEqualizerPreset;

interface IMediaServiceCallback {

    void updateSongInfo(in MediaMetaData mediaMetaData);

    void onProgressChange(long progress, long duration);

    void onSongChange(in SongInfo songInfo, int position);

    void onPlaybackStarted(boolean isStarted);

    void onShuffleModeChange(boolean isShuffleEnabled);

    void onSourceChange(String sourceType);

    void updateSourceInfo(in List<SourceInfo> sourceInfo);

    void onFavoriteChange(boolean isFavoriteEnabled);

    void onSettingsChange(in List<MediaSettings> mediaSettings);

    void onMediaEqualizerChange(in MediaEqualizerSettings mediaEqualizerSettings);

    void onPresetChange(in MediaEqualizerPreset mediaEqualizerPreset);

    void onBandLevelChange(int bandLevel);

}
