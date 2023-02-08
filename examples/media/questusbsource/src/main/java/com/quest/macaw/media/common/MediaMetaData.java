package com.quest.macaw.media.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MediaMetaData implements Parcelable {

    private boolean mIsPlaying = false;

    private boolean mIsShuffleEnabled = false;

    private int mCurrentSongPosition = 0;

    private SongInfo mCurrentSong;

    private List<SongInfo> mSongInfoList = new ArrayList<>();

    public MediaMetaData() {
    }


    protected MediaMetaData(Parcel in) {
        mIsPlaying = in.readByte() != 0;
        mIsShuffleEnabled = in.readByte() != 0;
        mCurrentSongPosition = in.readInt();
        mCurrentSong = in.readParcelable(SongInfo.class.getClassLoader());
        mSongInfoList = in.createTypedArrayList(SongInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mIsPlaying ? 1 : 0));
        dest.writeByte((byte) (mIsShuffleEnabled ? 1 : 0));
        dest.writeInt(mCurrentSongPosition);
        dest.writeParcelable(mCurrentSong, flags);
        dest.writeTypedList(mSongInfoList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaMetaData> CREATOR = new Creator<MediaMetaData>() {
        @Override
        public MediaMetaData createFromParcel(Parcel in) {
            return new MediaMetaData(in);
        }

        @Override
        public MediaMetaData[] newArray(int size) {
            return new MediaMetaData[size];
        }
    };

    public boolean isIsPlaying() {
        return mIsPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.mIsPlaying = isPlaying;
    }

    public boolean isIsShuffleEnabled() {
        return mIsShuffleEnabled;
    }

    public void setIsShuffleEnabled(boolean isShuffleEnabled) {
        this.mIsShuffleEnabled = isShuffleEnabled;
    }

    public int getCurrentSongPosition() {
        return mCurrentSongPosition;
    }

    public void setCurrentSongPosition(int currentSongPosition) {
        this.mCurrentSongPosition = currentSongPosition;
    }

    public SongInfo getCurrentSong() {
        return mCurrentSong;
    }

    public void setCurrentSong(SongInfo currentSong) {
        this.mCurrentSong = currentSong;
    }

    public List<SongInfo> getSongInfoList() {
        return mSongInfoList;
    }

    public void setSongInfoList(List<SongInfo> songInfoList) {
        this.mSongInfoList = songInfoList;
    }
}