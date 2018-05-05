package com.semisonfire.cloudgallery.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "photo")
public class Photo implements Parcelable, Comparable {

    @SerializedName("resource_id")
    @PrimaryKey
    @NonNull
    private String id = "";

    @SerializedName("name")
    @ColumnInfo(name = "name")
    private String name;

    @SerializedName("preview")
    @ColumnInfo(name = "preview")
    private String preview;

    @ColumnInfo(name = "removed")
    private boolean isRemoved;

    @ColumnInfo(name = "offline")
    private boolean isOffline;

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

    //Only trash
    @SerializedName("origin_path")
    @Ignore
    private String originPath;

    @SerializedName("modified")
    @ColumnInfo(name = "modified_at")
    private String modifiedAt;

    @Ignore
    private boolean isSelected;

    public Photo() {}

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
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

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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
            return name.equals(p.getName()) && getPreview().equals(p.getPreview());
        }

        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Photo photo = (Photo) o;
        return name.compareTo(photo.getName());
    }

    protected Photo(Parcel in) {
        id = in.readString();
        name = in.readString();
        preview = in.readString();
        isRemoved = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        isUploaded = in.readByte() != 0;
        isSelected = in.readByte() != 0;
        localPath = in.readString();
        remotePath = in.readString();
        mediaType = in.readString();
        originPath = in.readString();
        modifiedAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(preview);
        dest.writeByte((byte) (isRemoved ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeByte((byte) (isUploaded ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(localPath);
        dest.writeString(remotePath);
        dest.writeString(mediaType);
        dest.writeString(originPath);
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
