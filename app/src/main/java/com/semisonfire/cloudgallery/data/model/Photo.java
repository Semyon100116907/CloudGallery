package com.semisonfire.cloudgallery.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "photo")
public class Photo implements Parcelable {

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

    @SerializedName("file")
    @ColumnInfo(name = "file")
    private String file;

    @ColumnInfo(name = "removed")
    private boolean isRemoved;

    @ColumnInfo(name = "offline")
    private boolean isOffline;

    @ColumnInfo(name = "upload")
    private boolean isUpload;

    @ColumnInfo(name = "uploading")
    private boolean isUploading;

    @ColumnInfo(name = "local_path")
    private String localPath;

    @SerializedName("path")
    @ColumnInfo(name = "remote_path")
    private String remotePath;

    @SerializedName("media_type")
    @Ignore
    private String mediaType;

    @SerializedName("created")
    @ColumnInfo(name = "created_at")
    private String createdAt;

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

    public String getPreview() {
        return preview;
    }

    public void setPreview(String imageUri) {
        preview = imageUri;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Photo) {
            Photo p = (Photo) obj;
            return id.equals(p.getId());
        }

        return super.equals(obj);
    }

    //region ParcelImpl
    @Ignore
    protected Photo(Parcel in) {
        id = in.readString();
        name = in.readString();
        preview = in.readString();
        file = in.readString();
        isRemoved = in.readByte() != 0;
        isOffline = in.readByte() != 0;
        isUpload = in.readByte() != 0;
        isUploading = in.readByte() != 0;
        localPath = in.readString();
        remotePath = in.readString();
        mediaType = in.readString();
        createdAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(preview);
        dest.writeString(file);
        dest.writeByte((byte) (isRemoved ? 1 : 0));
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeByte((byte) (isUpload ? 1 : 0));
        dest.writeByte((byte) (isUploading ? 1 : 0));
        dest.writeString(localPath);
        dest.writeString(remotePath);
        dest.writeString(mediaType);
        dest.writeString(createdAt);
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
    //endregion
}
