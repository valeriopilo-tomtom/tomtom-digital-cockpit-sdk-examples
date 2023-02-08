package com.quest.macaw.media.appservice.data;

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.util.Log;

import androidx.annotation.NonNull;

import com.quest.macaw.media.appservice.data.interfaces.MusicPlayer;
import com.quest.macaw.media.common.MediaEqualizerPreset;
import com.quest.macaw.media.common.MediaEqualizerSettings;

public class EqualizerManager {

    private static final String TAG = "EqualizerManager";
    private final int PRESET_INDEX_CUSTOM = -1;
    private BassBoost mBass;
    private Equalizer mEqualizer;
    private PresetReverb mPresetReverb;
    private MediaEqualizerSettings mMediaEqualizerSettings;
    private MediaEqualizerDatabase mMediaEqualizerDatabase;
    private MusicPlayer mMusicPlayer;

    public EqualizerManager(){
    }

    public EqualizerManager(@NonNull MusicPlayer musicPlayer, Context context) {
        mMusicPlayer = musicPlayer;
        mBass = new BassBoost(0, musicPlayer.getAudioSessionId());
        mEqualizer = new Equalizer(0, musicPlayer.getAudioSessionId());
        mPresetReverb = new PresetReverb(0, musicPlayer.getAudioSessionId());
        mPresetReverb.setEnabled(true);
        mEqualizer.setEnabled(true);
        mBass.setEnabled(true);
        mMediaEqualizerDatabase = MediaEqualizerDatabase.getInstance(context);
    }

    public void setBandLevels(int band, int level) {
        Log.i("preset set band value","band= "+band +"," +level);
       try{
           mEqualizer.setBandLevel((short) band, (short) level);
       } catch (Exception e){
           Log.e("EqualizerManager","setBandLevels exception"+ e.getMessage());
       }
    }

    public void setAllBandLevels(int[] bandLevels) {
        Log.i("setAllBandLevels","preset= "+bandLevels[0]+","+bandLevels[1]+","+bandLevels[2]+","+bandLevels[3]+","+bandLevels[4]);
        try{

                short firstBand = (short) bandLevels[0];
                short secondBand = (short) bandLevels[1];
                short thirdBand = (short) bandLevels[2];
                short fourthBand = (short) bandLevels[3];
                short fifthBand = (short) bandLevels[4];

                mEqualizer.setBandLevel((short) 0, firstBand);
                mEqualizer.setBandLevel((short) 1, secondBand);
                mEqualizer.setBandLevel((short) 2, thirdBand);
                mEqualizer.setBandLevel((short) 3, fourthBand);
                mEqualizer.setBandLevel((short) 4, fifthBand);

                Log.i("setAllBandLevels preset set band value", "band= 0 " + bandLevels[0]);

        } catch (Exception e){
            Log.e("EqualizerManager","setBandLevels exception"+ e.getMessage());
        }
    }

    public void setSelectedPreset(int preset){
        Log.i("preset set selected preset","preset= "+preset);
        try{
            mEqualizer.release();
            mEqualizer = null;
            mEqualizer = new Equalizer(0, mMusicPlayer.getAudioSessionId());
            mEqualizer.usePreset((short) preset);
        }catch (Exception e){
            Log.e("EqualizerManager","setSelectedPreset exception"+ e.getMessage());
        }
    }

    public MediaEqualizerPreset loadCustomPreset(MediaEqualizerSettings mediaEqualizerSettings){
        MediaEqualizerPreset mediaEqualizerPreset = new MediaEqualizerPreset();

        mediaEqualizerPreset.setNumberOfPresets(mEqualizer.getNumberOfPresets());
        mediaEqualizerPreset.setLowerBandLevelRange(mEqualizer.getBandLevelRange()[0]);
        mediaEqualizerPreset.setUpperBandLevelRange(mEqualizer.getBandLevelRange()[1]);
        mediaEqualizerPreset.setBandFrequencyRange(mEqualizer.getBandFreqRange((short) 1));
        mediaEqualizerPreset.setCurrentPreset(PRESET_INDEX_CUSTOM);

        int[] bandLevels = new int[mEqualizer.getNumberOfBands()];

        for(int i=0;i<mEqualizer.getNumberOfBands();i++){
            bandLevels[i]  = mEqualizer.getBandLevel((short) i);
        }
        bandLevels[0]= mediaEqualizerSettings.getFirstBand();
        bandLevels[1]=mediaEqualizerSettings.getSecondBand();
        bandLevels[2]= mediaEqualizerSettings.getThirdBand();
        bandLevels[3]= mediaEqualizerSettings.getFourthBand();
        bandLevels[4]= mediaEqualizerSettings.getFifthBand();

        mediaEqualizerPreset.setBandCurrentLevels(bandLevels);

        int m = mEqualizer.getNumberOfPresets();
        String[] presetList = new String[m];
        for (int k = 0; k < m; k++) {
            presetList[k] = mEqualizer.getPresetName((short) k);
        }
        mediaEqualizerPreset.setPresetName(presetList);
        return mediaEqualizerPreset;

    }
    private MediaEqualizerPreset loadPredefinedPreset(MediaEqualizerSettings mediaEqualizerSettings,Equalizer equalizer){

        MediaEqualizerPreset mediaEqualizerPreset = new MediaEqualizerPreset();
        mediaEqualizerPreset.setNumberOfPresets(equalizer.getNumberOfPresets());
        mediaEqualizerPreset.setLowerBandLevelRange(equalizer.getBandLevelRange()[0]);
        mediaEqualizerPreset.setUpperBandLevelRange(equalizer.getBandLevelRange()[1]);
        mediaEqualizerPreset.setBandFrequencyRange(equalizer.getBandFreqRange((short) 1));
        mediaEqualizerPreset.setCurrentPreset(equalizer.getCurrentPreset());

        int[] value = equalizer.getBandFreqRange((short) 1);

        int[] bandLevels = new int[equalizer.getNumberOfBands()];

        for(int i=0;i<equalizer.getNumberOfBands();i++){
            bandLevels[i]  = equalizer.getBandLevel((short) i);
        }

        mediaEqualizerPreset.setBandCurrentLevels(bandLevels);

        int m = equalizer.getNumberOfPresets();
        String[] presetList = new String[m];
        for (int k = 0; k < m; k++) {
            presetList[k] = equalizer.getPresetName((short) k);
        }
        mediaEqualizerPreset.setPresetName(presetList);
        return mediaEqualizerPreset;
    }

    public MediaEqualizerPreset getEqualizerProperties(){
        MediaEqualizerPreset mediaEqualizerPreset = new MediaEqualizerPreset();

        MediaEqualizerSettings mediaEqualizerSettings = mMediaEqualizerDatabase.mMediaDao.getMediaEqualizerSettings();
        int presetIndex = mediaEqualizerSettings.getPresetIndex();
        if(presetIndex == -1){
            mediaEqualizerPreset = loadCustomPreset(mediaEqualizerSettings);
        }
        else {
            mEqualizer.release();
            mEqualizer = null;
            mEqualizer = new Equalizer(0, mMusicPlayer.getAudioSessionId());
            mEqualizer.usePreset((short) presetIndex);
            mediaEqualizerPreset = loadPredefinedPreset(mediaEqualizerSettings,mEqualizer);

        }
        return mediaEqualizerPreset;

    }
    //TODO need to check an update
    public MediaEqualizerSettings getMediaEqualizerSettingsFromEqualizer(){

        MediaEqualizerSettings mediaEqualizerSettings = mMediaEqualizerDatabase.mMediaDao.getMediaEqualizerSettings();
        mediaEqualizerSettings.setPresetIndex(mEqualizer.getCurrentPreset());
        mediaEqualizerSettings.setFirstBand(mEqualizer.getBandLevel((short) 0));
        mediaEqualizerSettings.setSecondBand(mEqualizer.getBandLevel((short) 1));
        mediaEqualizerSettings.setThirdBand(mEqualizer.getBandLevel((short) 2));
        mediaEqualizerSettings.setFourthBand(mEqualizer.getBandLevel((short) 3));
        mediaEqualizerSettings.setFifthBand(mEqualizer.getBandLevel((short) 4));

        Log.i(TAG,"equalizer manager getCurrentPreset"+mEqualizer.getCurrentPreset() +","+mEqualizer.getBandLevel((short) 0) +","+mEqualizer.getBandLevel((short) 1));

        return mediaEqualizerSettings;

    }
}



