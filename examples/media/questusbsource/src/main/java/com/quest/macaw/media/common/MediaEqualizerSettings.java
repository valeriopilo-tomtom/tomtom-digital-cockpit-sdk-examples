package com.quest.macaw.media.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "MediaEqualizerSettings")
public class MediaEqualizerSettings implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int Id;

    @ColumnInfo(name = "first_band")
    private int mFirstBand;

    @ColumnInfo(name = "second_band")
    private int mSecondBand;

    @ColumnInfo(name = "third_band")
    private int mThirdBand;

    @ColumnInfo(name = "fourth_band")
    private int mFourthBand;

    @ColumnInfo(name = "fifth_band")
    private int mFifthBand;

    @ColumnInfo(name = "preset_index")
    private int mPresetIndex;

    @Ignore
    private int[] mBandLevels;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getFirstBand() {
        return mFirstBand;
    }

    public void setFirstBand(int firstBand) {
        this.mFirstBand = firstBand;
    }

    public int getSecondBand() {
        return mSecondBand;
    }

    public void setSecondBand(int secondBand) {
        this.mSecondBand = secondBand;
    }

    public int getThirdBand() {
        return mThirdBand;
    }

    public void setThirdBand(int thirdBand) {
        this.mThirdBand = thirdBand;
    }

    public int getFourthBand() {
        return mFourthBand;
    }

    public void setFourthBand(int fourthBand) {
        this.mFourthBand = fourthBand;
    }

    public int getFifthBand() {
        return mFifthBand;
    }

    public void setFifthBand(int fifthBand) {
        this.mFifthBand = fifthBand;
    }

    public int getPresetIndex() {
        return mPresetIndex;
    }

    public void setPresetIndex(int presetIndex) {
        this.mPresetIndex = presetIndex;
    }

    public int[] getBandLevels() {
        return mBandLevels;
    }

    public void setBandLevels(int[] bandLevels) {
        this.mBandLevels = bandLevels;
    }

    public MediaEqualizerSettings(int Id, int firstBand, int secondBand, int thirdBand, int fourthBand, int fifthBand, int presetIndex) {
        this.Id = Id;
        this.mFirstBand = firstBand;
        this.mSecondBand = secondBand;
        this.mThirdBand = thirdBand;
        this.mFourthBand = fourthBand;
        this.mFifthBand = fifthBand;
        this.mPresetIndex = presetIndex;
    }

    protected MediaEqualizerSettings(Parcel in) {
        Id = in.readInt();
        mFirstBand = in.readInt();
        mSecondBand = in.readInt();
        mThirdBand = in.readInt();
        mFourthBand = in.readInt();
        mFifthBand = in.readInt();
        mPresetIndex = in.readInt();
        mBandLevels = in.createIntArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeInt(mFirstBand);
        dest.writeInt(mSecondBand);
        dest.writeInt(mThirdBand);
        dest.writeInt(mFourthBand);
        dest.writeInt(mFifthBand);
        dest.writeInt(mPresetIndex);
        dest.writeIntArray(mBandLevels);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaEqualizerSettings> CREATOR = new Creator<MediaEqualizerSettings>() {
        @Override
        public MediaEqualizerSettings createFromParcel(Parcel in) {
            return new MediaEqualizerSettings(in);
        }

        @Override
        public MediaEqualizerSettings[] newArray(int size) {
            return new MediaEqualizerSettings[size];
        }
    };
}
