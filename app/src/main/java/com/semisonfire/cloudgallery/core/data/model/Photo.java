package com.semisonfire.cloudgallery.core.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "photo")
public class Photo implements Parcelable, Comparable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @SerializedName("name")
    @ColumnInfo(name = "name")
    private String name;

    @SerializedName("preview")
    @ColumnInfo(name = "preview")
    private String preview;

    @ColumnInfo(name = "upload")
    private boolean isUploaded;

    @ColumnInfo(name = "local_path")
    private String localPath;

    @SerializedName("file")
    @ColumnInfo(name = "file")
    private String file;

    @SerializedName("path")
    @ColumnInfo(name = "remote_path")
    private String remotePath;

    @SerializedName("media_type")
    @Ignore
    private String mediaType;

    @SerializedName("modified")
    @ColumnInfo(name = "modified_at")
    private String modifiedAt;

    @Ignore
    private boolean isSelected;

    public Photo() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPreview() {
        return preview != null ? preview : file;
    }

    public void setPreview(String imageUri) {
        preview = imageUri;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Photo) {
            Photo p = (Photo) obj;
            return name.equals(p.getName());
        }

        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Photo photo = (Photo) o;
        return name.compareTo(photo.getName());
    }

    protected Photo(Parcel in) {
        name = in.readString();
        preview = in.readString();
        isUploaded = in.readByte() != 0;
        isSelected = in.readByte() != 0;
        localPath = in.readString();
        remotePath = in.readString();
        mediaType = in.readString();
        modifiedAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(preview);
        dest.writeByte((byte) (isUploaded ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(localPath);
        dest.writeString(remotePath);
        dest.writeString(mediaType);
        dest.writeString(modifiedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
