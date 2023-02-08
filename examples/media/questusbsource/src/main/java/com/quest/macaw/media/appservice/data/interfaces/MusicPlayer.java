package com.quest.macaw.media.appservice.data.interfaces;

import android.net.Uri;

import com.quest.macaw.media.appservice.data.player.MusicPlayerImpl;
import com.quest.macaw.media.common.SongInfo;

import java.util.List;

public interface MusicPlayer {

    void prepare(List<SongInfo> songInfoList, boolean playWhenReady);

    void updateMediaSource(List<SongInfo> songInfoList);

    void loadMusic(String mediaId);

    void play();

    void pause();

    void shuffle(boolean isEnabled);

    void loop(boolean isEnabled);

    void nextTrac();

    void previousTrack();

    void favourite(boolean isEnabled);

    void seekTo(long progress);

    void setOnProgressChangeListener(MusicPlayerImpl.MediaPlayerListener mMediaPlayerListener);

    void release();

    int getAudioSessionId();

    void prepare(Uri uri);

    void stop();

    void setVolume(float multiplier);

    void startOver();
}
