package com.quest.macaw.media.common;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class SongInfo implements Parcelable{

    private String mMediaId = "";

    private String mTitle;

    private String mAlbumName;

    private String mArtistName;

    private String mCoverArtUrl;

    private String mProviderLogoUrl;

    private String mDisplayName;

    private Long mDuration;

    private Long mAlbumId;

    private boolean mIsFavorite;

    private boolean mIsLocalPath = false;

    private Uri mSongUri;

    private boolean isAlexaSource = false;

    private Bitmap mCovertArtImage;

    public SongInfo() {

    }


    protected SongInfo(Parcel in) {
        mMediaId = in.readString();
        mTitle = in.readString();
        mAlbumName = in.readString();
        mArtistName = in.readString();
        mCoverArtUrl = in.readString();
        mProviderLogoUrl = in.readString();
        mDisplayName = in.readString();
        if (in.readByte() == 0) {
            mDuration = null;
        } else {
            mDuration = in.readLong();
        }
        if (in.readByte() == 0) {
            mAlbumId = null;
        } else {
            mAlbumId = in.readLong();
        }
        mIsFavorite = in.readByte() != 0;
        mIsLocalPath = in.readByte() != 0;
        mSongUri = in.readParcelable(Uri.class.getClassLoader());
        isAlexaSource = in.readByte() != 0;
        mCovertArtImage = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<SongInfo> CREATOR = new Creator<SongInfo>() {
        @Override
        public SongInfo createFromParcel(Parcel in) {
            return new SongInfo(in);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };

    public String getMediaId() {
        return mMediaId;
    }

    public void setMediaId(String mediaId) {
        this.mMediaId = mediaId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        this.mArtistName = artistName;
    }

    public Long getDuration() {
        return mDuration;
    }

    public void setDuration(Long duration) {
        this.mDuration = duration;
    }

    public Long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(Long albumId) {
        this.mAlbumId = albumId;
    }

    public String getCoverArtUrl() {
        return mCoverArtUrl;
    }

    public void setCoverArtUrl(String coverArtUrl) {
        this.mCoverArtUrl = coverArtUrl;
    }

    public Uri getSongUri() {
        return mSongUri;
    }

    public void setSongUri(Uri songUri) {
        this.mSongUri = songUri;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.mIsFavorite = isFavorite;
    }

    public boolean isLocalPath() {
        return mIsLocalPath;
    }

    public void setIsLocalPath(boolean isLocalPath) {
        this.mIsLocalPath = isLocalPath;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getProviderLogoUrl() {
        return mProviderLogoUrl;
    }

    public void setProviderLogoUrl(String mProviderLogoUrl) {
        this.mProviderLogoUrl = mProviderLogoUrl;
    }


    @Override
    public String toString() {
        return "SongInfo{" +
                "mMediaId='" + mMediaId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mAlbumName='" + mAlbumName + '\'' +
                ", mArtistName='" + mArtistName + '\'' +
                ", mCoverArtUrl='" + mCoverArtUrl + '\'' +
                ", mDisplayName='" + mDisplayName + '\'' +
                ", mDuration=" + mDuration +
                ", mAlbumId=" + mAlbumId +
                ", mIsFavorite=" + mIsFavorite +
                ", mIsLocalPath=" + mIsLocalPath +
                ", mSongUri=" + mSongUri +
                ", isAlexaSource=" + isAlexaSource +
                ", mCovertArtImage=" + mCovertArtImage +
                ", mProviderLogoUrl=" + mProviderLogoUrl +
                '}';
    }

    public boolean getIsAlexaSource() {
        return isAlexaSource;
    }

    public void setIsAlexaSource(boolean alexaSource) {
        isAlexaSource = alexaSource;
    }

    public Bitmap getCovertArtImage() {
        return mCovertArtImage;
    }

    public void setCovertArtImage(Bitmap mCovertArtImage) {
        this.mCovertArtImage = mCovertArtImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMediaId);
        dest.writeString(mTitle);
        dest.writeString(mAlbumName);
        dest.writeString(mArtistName);
        dest.writeString(mCoverArtUrl);
        dest.writeString(mProviderLogoUrl);
        dest.writeString(mDisplayName);
        if (mDuration == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(mDuration);
        }
        if (mAlbumId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(mAlbumId);
        }
        dest.writeByte((byte) (mIsFavorite ? 1 : 0));
        dest.writeByte((byte) (mIsLocalPath ? 1 : 0));
        dest.writeParcelable(mSongUri, flags);
        dest.writeByte((byte) (isAlexaSource ? 1 : 0));
        dest.writeParcelable(mCovertArtImage, flags);
    }
}
