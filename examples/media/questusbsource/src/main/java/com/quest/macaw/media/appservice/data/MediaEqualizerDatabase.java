package com.quest.macaw.media.appservice.data;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.room.Room;

import com.quest.macaw.media.appservice.data.database.MediaDao;
import com.quest.macaw.media.appservice.data.database.MediaDatabase;
import com.quest.macaw.media.common.IMediaServiceCallback;
import com.quest.macaw.media.common.MediaEqualizerSettings;

public class MediaEqualizerDatabase {

    private static final String TAG = "MediaEqualizerDatabase";
    public MediaDao mMediaDao;
    private RemoteCallbackList<IMediaServiceCallback> mMediaServiceCallback = new RemoteCallbackList<>();
    private EqualizerManager mEqualizerManager;
    private static MediaEqualizerDatabase mMediaEqualizerDatabase;
    private MediaDatabase mMediaDatabase;

    public void registerCallback(IMediaServiceCallback mediaServiceCallback) {
        mMediaServiceCallback.register(mediaServiceCallback);
    }

    public void unregisterCallback(IMediaServiceCallback mediaServiceCallback) {
        mMediaServiceCallback.unregister(mediaServiceCallback);
    }

    public static MediaEqualizerDatabase getInstance(Context context) {
        if (mMediaEqualizerDatabase == null) {
            mMediaEqualizerDatabase = new MediaEqualizerDatabase();
        }
        return mMediaEqualizerDatabase;
    }

    public void createDatabase(Context context) {
        if (mMediaDatabase == null) {
            mMediaDatabase = Room.databaseBuilder(context, MediaDatabase.class,
                    "room_db").build();
            mMediaDao = mMediaDatabase.mediaDao();
            new Thread(() -> {
                if (mMediaDao.getMediaEqualizerSettings() == null) {
                    insertCustomBandLevels();
                }
            }).start();
        }
    }

    public void insertCustomBandLevels() {
        new Thread(() -> {
            int id = 1;
            int first_band = 0;
            int second_band = 0;
            int third_band = 0;
            int fourth_band = 0;
            int fifth_band = 0;
            int preset_index = 1;
            MediaEqualizerSettings data = new MediaEqualizerSettings(id, first_band, second_band, third_band, fourth_band, fifth_band, preset_index);
            mMediaDao.insertDetails(data);
        }).start();
    }

    public void getCustomBandLevels() {
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            /*if (mediaEqualizer != null) {
                if (mMediaServiceCallback != null) {
                    try {
                        mMediaServiceCallback.onChangeAudio(mediaEqualizer);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            } else {
                Log.e(TAG, "Table value is null");
            }*/
        }).start();
    }

    public void insertAllData(MediaEqualizerSettings data) {
        Log.i(TAG, "insert all data call");
        new Thread(() -> mMediaDao.insertDetails(data)).start();
    }

    private void updateAllData(MediaEqualizerSettings mediaEqualizerData) {
        Log.i(TAG, "update all data call"+mediaEqualizerData.getFirstBand());
        new Thread(() -> {
            mMediaDao.updateDetails(mediaEqualizerData);
            int preset = mediaEqualizerData.getPresetIndex();
            // MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizerData != null) {
                if (mMediaServiceCallback != null) {
                    Log.i(TAG, "mMediaServiceCallback call" + mMediaServiceCallback.getClass().toString());
                    if (preset != -1) {
                        //TODO need to check an update
                        updateEqualizerData(mediaEqualizerData);
                            /*EqualizerManager equalizerManager = new EqualizerManager();
                            MediaEqualizerSettings mediaEqualizerSettings =mEqualizerManager.getMediaEqualizerSettingsFromEqualizer();
                            mMediaServiceCallback.onMediaEqualizerChange(mediaEqualizerSettings);*/
                    } else {
                        Log.i(TAG, "media equalizer data" + mediaEqualizerData.getFirstBand() + "," + mediaEqualizerData.getFirstBand());
                        updateEqualizerData(mediaEqualizerData);
                    }
                }
            } else {
                Log.e(TAG, "Table value is null");
            }
        }).start();
    }

    private void updateEqualizerData(MediaEqualizerSettings mediaEqualizerData) {
        synchronized (mMediaServiceCallback) {
            int n = mMediaServiceCallback.beginBroadcast();
            Log.i(TAG, "updateEqualizerData callback count: " + n);
            try {
                for (int i = 0; i < n; i++) {
                    Log.i(TAG, ">updateEqualizerData: " + i);
                    mMediaServiceCallback.getBroadcastItem(i).onMediaEqualizerChange(mediaEqualizerData);
                    Log.i(TAG, "<updateEqualizerData: " + i);
                }
            } catch (RemoteException ex) {
                Log.i(TAG, "updateEqualizerData: " + ex.getMessage());
            } finally {
                mMediaServiceCallback.finishBroadcast();
            }
        }
    }

    public void updateFirstBand(int firstBand) {
        Log.e(TAG, "update first band" + firstBand);
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                mediaEqualizer.setFirstBand(firstBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "update first band" + firstBand);
            } else {
                Log.e(TAG, "Table first band" + firstBand);
            }
            //Log.e(TAG, "update first band" + firstBand);
        }).start();
    }

    public void updateSecondBand(int secondBand) {
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                mediaEqualizer.setSecondBand(secondBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "update second band" + secondBand);
            } else {
                Log.e(TAG, "Table has null value");
            }
        }).start();
    }

    public void updateThirdBand(int thirdBand) {
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                mediaEqualizer.setThirdBand(thirdBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "update third band" + thirdBand);
            } else {
                Log.e(TAG, "Table has null value");
            }
        }).start();
    }

    public void updateFourthBand(int fourthBand) {
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                mediaEqualizer.setFourthBand(fourthBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "update fourth band" + fourthBand);
            } else {
                Log.e(TAG, "Table has null value");
            }
        }).start();
    }

    public void updateFifthBand(int fifthBand) {
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                mediaEqualizer.setFifthBand(fifthBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "update fifth band" + fifthBand);
            } else {
                Log.e(TAG, "Table has null value");
            }
        }).start();
    }

    public void updateAllBandsLevels(int[] bandLevels){
        new Thread(() -> {
            MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
            if (mediaEqualizer != null) {
                //mediaEqualizer.setBandLevels(bandLevels);
                mediaEqualizer.setFirstBand(bandLevels[0]);
                mediaEqualizer.setSecondBand(bandLevels[1]);
                mediaEqualizer.setThirdBand(bandLevels[2]);
                mediaEqualizer.setFourthBand(bandLevels[3]);
                mediaEqualizer.setFifthBand(bandLevels[4]);
                //mediaEqualizer.setFifthBand(fifthBand);
                mediaEqualizer.setPresetIndex(-1);
                updateAllData(mediaEqualizer);
                Log.e(TAG, "updateAllBandsLevels" +bandLevels[0] +","+bandLevels[1]);
            } else {
                Log.e(TAG, "Table has null value");
            }
        }).start();
    }

    public void updatePresetIndex(int presetIndex) {
        new Thread(() -> {
        MediaEqualizerSettings mediaEqualizer = mMediaDao.getMediaEqualizerSettings();
        if (mediaEqualizer != null) {
            mediaEqualizer.setPresetIndex(presetIndex);
            updateAllData(mediaEqualizer);
            Log.e(TAG, "update preset index" + presetIndex);
        } else {
            Log.e(TAG, "Table has null value");
        }
        }).start();
    }
}
