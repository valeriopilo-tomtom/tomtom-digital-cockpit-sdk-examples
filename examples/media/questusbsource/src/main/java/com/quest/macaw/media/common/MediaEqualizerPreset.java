package com.quest.macaw.media.common;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaEqualizerPreset implements Parcelable {

    private int mBandLevel;

    private int mNumberOfPresets;

    private String[] mPresetName;

    private int mLowerBandLevelRange;

    private int mUpperBandLevelRange;

    private int[] mBandFrequencyRange;

    private int[] mBandCurrentLevels;

    private int mCurrentPreset;

    public MediaEqualizerPreset() {

    }

    protected MediaEqualizerPreset(Parcel in) {
        mBandLevel = in.readInt();
        mNumberOfPresets = in.readInt();
        mPresetName = in.createStringArray();
        mBandFrequencyRange = in.createIntArray();
        mLowerBandLevelRange = in.readInt();
        mUpperBandLevelRange = in.readInt();
        mBandCurrentLevels = in.createIntArray();
        mCurrentPreset = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBandLevel);
        dest.writeInt(mNumberOfPresets);
        dest.writeStringArray(mPresetName);
        dest.writeIntArray(mBandFrequencyRange);
        dest.writeInt(mLowerBandLevelRange);
        dest.writeInt(mUpperBandLevelRange);
        dest.writeIntArray(mBandCurrentLevels);
        dest.writeInt(mCurrentPreset);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaEqualizerPreset> CREATOR = new Creator<MediaEqualizerPreset>() {
        @Override
        public MediaEqualizerPreset createFromParcel(Parcel in) {
            return new MediaEqualizerPreset(in);
        }

        @Override
        public MediaEqualizerPreset[] newArray(int size) {
            return new MediaEqualizerPreset[size];
        }
    };

    public int getBandLevel() {
        return mBandLevel;
    }

    public void setBandLevel(int bandLevel) {
        this.mBandLevel = bandLevel;
    }

    public int getNumberOfPresets() {
        return mNumberOfPresets;
    }

    public void setNumberOfPresets(int numberOfPresets) {
        this.mNumberOfPresets = numberOfPresets;
    }

    public String[] getPresetName() {
        return mPresetName;
    }

    public void setPresetName(String[] presetName) {
        this.mPresetName = presetName;
    }

    public int getLowerBandLevelRange() {
        return mLowerBandLevelRange;
    }

    public void setLowerBandLevelRange(int lowerBandLevelRange) {
        this.mLowerBandLevelRange = lowerBandLevelRange;
    }

    public int getUpperBandLevelRange() {
        return mUpperBandLevelRange;
    }

    public void setUpperBandLevelRange(int upperBandLevelRange) {
        this.mUpperBandLevelRange = upperBandLevelRange;
    }

    public int[] getBandFrequencyRange() {
        return mBandFrequencyRange;
    }

    public void setBandFrequencyRange(int[] bandFrequencyRange) {
        this.mBandFrequencyRange = bandFrequencyRange;
    }

    public int[] getBandCurrentLevels() {
        return mBandCurrentLevels;
    }

    public void setBandCurrentLevels(int[] bandCurrentLevels) {
        this.mBandCurrentLevels = bandCurrentLevels;
    }

    public int getCurrentPreset() {
        return mCurrentPreset;
    }

    public void setCurrentPreset(int currentPreset) {
        this.mCurrentPreset = currentPreset;
    }
}
