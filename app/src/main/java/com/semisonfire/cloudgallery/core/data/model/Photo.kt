package com.semisonfire.cloudgallery.core.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Entity(tableName = "photo")
class Photo : Parcelable {

  @PrimaryKey(autoGenerate = true)
  @SerializedName("resource_id")
  var id: String = ""

  var name: String = ""

  var preview: String = ""

  @ColumnInfo(name = "upload")
  var isUploaded = false

  @ColumnInfo(name = "local_path")
  var localPath: String = ""

  var file: String = ""

  @SerializedName("path")
  @ColumnInfo(name = "remote_path")
  var remotePath: String = ""

  @SerializedName("media_type")
  @Ignore
  var mediaType: String = ""
    private set

  @SerializedName("modified")
  @ColumnInfo(name = "modified_at")
  var modifiedAt: String = ""

  @Ignore
  var isSelected = false

  constructor()

  protected constructor(`in`: Parcel) {
    name = `in`.readString() ?: ""
    preview = `in`.readString() ?: ""
    isUploaded = `in`.readByte().toInt() != 0
    isSelected = `in`.readByte().toInt() != 0
    localPath = `in`.readString() ?: ""
    remotePath = `in`.readString() ?: ""
    mediaType = `in`.readString() ?: ""
    modifiedAt = `in`.readString() ?: ""
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(name)
    dest.writeString(preview)
    dest.writeByte((if (isUploaded) 1 else 0).toByte())
    dest.writeByte((if (isSelected) 1 else 0).toByte())
    dest.writeString(localPath)
    dest.writeString(remotePath)
    dest.writeString(mediaType)
    dest.writeString(modifiedAt)
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Photo) return false

    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  companion object CREATOR : Parcelable.Creator<Photo> {
    override fun createFromParcel(parcel: Parcel): Photo {
      return Photo(parcel)
    }

    override fun newArray(size: Int): Array<Photo?> {
      return arrayOfNulls(size)
    }
  }
}