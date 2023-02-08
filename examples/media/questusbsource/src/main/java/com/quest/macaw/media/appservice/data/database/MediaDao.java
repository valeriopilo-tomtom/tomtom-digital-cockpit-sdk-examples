package com.quest.macaw.media.appservice.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.quest.macaw.media.common.MediaEqualizerSettings;

@Dao
public interface MediaDao {

    @Insert
    void insertDetails(MediaEqualizerSettings data);

    @Query("select * from MediaEqualizerSettings")
    MediaEqualizerSettings getMediaEqualizerSettings();

    @Update
    void updateDetails(MediaEqualizerSettings data);
}
