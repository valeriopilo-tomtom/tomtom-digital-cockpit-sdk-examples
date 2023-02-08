package com.quest.macaw.media.appservice.data.interfaces;

import com.quest.macaw.media.common.MediaInterfaceConstant;
import com.quest.macaw.media.common.SongInfo;

import java.util.List;

public interface Sources {

    MediaInterfaceConstant.SourceType getType();

    MediaInterfaceConstant.SourceState getState();

    String getPath();

    List<SongInfo> getSongs();

    void changeSource();

    default void setSongInfo(SongInfo songInfo){}

    default void setSourceState(MediaInterfaceConstant.SourceState disconnected){}
}
