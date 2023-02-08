package com.quest.macaw.media.common;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaSettings implements Parcelable {

    private int mIcon;

    private String mTitle;

    private String mDescription;

    private boolean mIsButtonEnabled;

    private boolean mIsArrowEnabled;

    private boolean mIsSettingsEnabled;

    public MediaSettings() {
    }

    protected MediaSettings(Parcel in) {
        mIcon = in.readInt();
        mTitle = in.readString();
        mDescription = in.readString();
        mIsButtonEnabled = in.readByte() != 0;
        mIsArrowEnabled = in.readByte() != 0;
        mIsSettingsEnabled = in.readByte() != 0;
    }

    public static final Creator<MediaSettings> CREATOR = new Creator<MediaSettings>() {
        @Override
        public MediaSettings createFromParcel(Parcel in) {
            return new MediaSettings(in);
        }

        @Override
        public MediaSettings[] newArray(int size) {
            return new MediaSettings[size];
        }
    };

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        this.mIcon = icon;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public boolean isButtonEnabled() {
        return mIsButtonEnabled;
    }

    public void setIsButtonEnabled(boolean isButtonEnabled) {
        this.mIsButtonEnabled = isButtonEnabled;
    }

    public boolean isArrowEnabled() {
        return mIsArrowEnabled;
    }

    public void setIsArrowEnabled(boolean isArrowEnabled) {
        this.mIsArrowEnabled = isArrowEnabled;
    }

    public boolean isSettingsEnabled() {
        return mIsSettingsEnabled;
    }

    public void setIsSettingsEnabled(boolean isSettingsEnabled) {
        this.mIsSettingsEnabled = isSettingsEnabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIcon);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeByte((byte) (mIsButtonEnabled ? 1 : 0));
        dest.writeByte((byte) (mIsArrowEnabled ? 1 : 0));
        dest.writeByte((byte) (mIsSettingsEnabled ? 1 : 0));
    }
}
