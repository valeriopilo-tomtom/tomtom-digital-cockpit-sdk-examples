package com.quest.macaw.media.appservice.data.interfaces;

import com.quest.macaw.media.common.MediaInterfaceConstant;

public interface OnSourceStateChangeListener {

    void onSourceStateChange(MediaInterfaceConstant.SourceType sourceType, MediaInterfaceConstant.SourceState sourceState);
}
