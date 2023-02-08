package com.quest.macaw.media.common;

import androidx.annotation.NonNull;

public class MediaInterfaceConstant {

    private MediaInterfaceConstant() {
        //Not used
    }

    public static final int MEDIA_CONTROL_ACTION_PLAY = 1;
    public static final int MEDIA_CONTROL_ACTION_PAUSE = 2;
    public static final int MEDIA_CONTROL_ACTION_NEXT = 3;
    public static final int MEDIA_CONTROL_ACTION_PREVIOUS = 4;
    public static final int MEDIA_CONTROL_ACTION_SHUFFLE_LOOP = 5;
    public static final int MEDIA_CONTROL_ACTION_FAVORITE = 6;
    public static final int MEDIA_CONTROL_ACTION_SONG_CHANGE = 7;
    public static final int MEDIA_CONTROL_ACTION_PROGRESS = 8;

    public static final String MEDIA_ID = "media_id";
    public static final String MEDIA_TITLE = "media_title";
    public static final String ACTION_MEDIA_SERVICE = "com.quest.macaw.media.appservice.MediaService";
    public static final String PACKAGE_MEDIA_SERVICE = "com.quest.macaw.media.appservice";
    public static final String PACKAGE_MEDIA = "com.quest.macaw.media";
    public static final String NAME_MEDIA_SERVICE = "com.quest.macaw.media.appservice.MediaService";
    public static final String WIDGET_ACTION_PREVIOUS = "com.quest.macaw.media.widget.ACTION_PREVIOUS";
    public static final String WIDGET_ACTION_NEXT = "com.quest.macaw.media.widget.ACTION_NEXT";
    public static final String WIDGET_ACTION_SONG_CHANGE = "com.quest.macaw.media.widget.ACTION_SONG_CHANGE";
    public static final String WIDGET_ACTION_GET_SONG = "com.quest.macaw.media.widget.ACTION_GET_SONG";
    public static final String WIDGET_ACTION_REGISTER = "com.quest.macaw.media.widget.ACTION_REGISTER";
    public static final String WIDGET_ACTION_UNREGISTER = "com.quest.macaw.media.widget.ACTION_UNREGISTER";

    public static final String INTERNAL_STORAGE_PATH = "/system/product/media/music";

    public enum SourceType {
        INTERNAL_MEMORY("INTERNAL_MEMORY"),
        USB1("USB1"),
        USB2("USB2"),
        BLUETOOTH("BLUETOOTH"),
        AM("AM"),
        FM("FM"),
        IPOD("IPOD"),
        ALEXA("ALEXA");

        String type;

        SourceType(String type) {
            this.type = type;
        }

        @NonNull
        @Override
        public String toString() {
            return type;
        }
    }

    public enum SourceState {
        INITIALIZING,
        CONNECTED,
        DISCONNECTED,
        READY,
        FAILD;
    }
}
