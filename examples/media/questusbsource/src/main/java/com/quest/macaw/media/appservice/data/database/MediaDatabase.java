package com.quest.macaw.media.appservice.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.quest.macaw.media.common.MediaEqualizerSettings;

//Creating Database with name:AudioDatabase and setting entity(table) as MediaEqualizerSettings
@Database(entities = {MediaEqualizerSettings.class}, version = 2, exportSchema = false)

public abstract class MediaDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();
}
