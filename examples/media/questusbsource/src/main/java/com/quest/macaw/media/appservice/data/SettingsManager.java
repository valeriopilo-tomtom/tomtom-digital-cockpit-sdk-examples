package com.quest.macaw.media.appservice.data;

import android.content.Context;

import com.quest.macaw.media.appservice.R;
import com.quest.macaw.media.common.MediaSettings;

import java.util.ArrayList;
import java.util.List;

public class SettingsManager {

    private final List<MediaSettings> mMediaSettings;

    public SettingsManager(Context context) {
        mMediaSettings = new ArrayList<>();
        MediaSettings settings1 = new MediaSettings();
        settings1.setTitle(context.getString(R.string.settings_main_animation_title));
        settings1.setDescription(context.getString(R.string.settings_main_animation_description));
        settings1.setIsButtonEnabled(true);
        settings1.setIsArrowEnabled(false);
        settings1.setIsSettingsEnabled(true);

        MediaSettings settings2 = new MediaSettings();
        settings2.setTitle(context.getString(R.string.settings_main_online_music_title));
        settings2.setDescription(context.getString(R.string.settings_main_online_music_description));
        settings2.setIsButtonEnabled(true);
        settings2.setIsArrowEnabled(false);
        settings2.setIsSettingsEnabled(false);

       /* MediaSettings settings3 = new MediaSettings();
        settings3.setTitle(context.getString(R.string.settings_main_equalizer_title));
        settings3.setDescription(context.getString(R.string.settings_main_equalizer_description));
        settings3.setIsButtonEnabled(false);
        settings3.setIsArrowEnabled(true);
        settings3.setIsSettingsEnabled(false);
*/
        mMediaSettings.add(settings1);
        mMediaSettings.add(settings2);
        //mMediaSettings.add(settings3);

    }

    public List<MediaSettings> getMediaSettings() {
        return mMediaSettings;
    }

    public void updateSettings(String settingsName, boolean settingsValue) {
        mMediaSettings.forEach(s -> {
            if (s.getTitle().equals(settingsName)) {
                s.setIsSettingsEnabled(settingsValue);
            }
        });
    }
}
