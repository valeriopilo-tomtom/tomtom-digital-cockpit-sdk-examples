package com.quest.macaw.media.common;

import android.os.Parcel;
import android.os.Parcelable;

public class SourceInfo implements Parcelable {

    private String mSource;

    private String mStatus;

    private boolean mIsActive;

    public SourceInfo() {
        //Default constructor.
    }

    protected SourceInfo(Parcel in) {
        mSource = in.readString();
        mStatus = in.readString();
        mIsActive = in.readByte() != 0;
    }

    public static final Creator<SourceInfo> CREATOR = new Creator<SourceInfo>() {
        @Override
        public SourceInfo createFromParcel(Parcel in) {
            return new SourceInfo(in);
        }

        @Override
        public SourceInfo[] newArray(int size) {
            return new SourceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSource);
        dest.writeString(mStatus);
        dest.writeByte((byte) (mIsActive ? 1 : 0));
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        this.mSource = source;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean isActive) {
        this.mIsActive = isActive;
    }
}
