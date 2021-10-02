package com.semisonfire.cloudgallery.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey
    @SerializedName("resource_id")
    val id: String = "",
    val name: String = "",
    val preview: String = "",
    val localPath: String = "",
    val file: String = "",
    @ColumnInfo(name = "upload")
    var isUploaded: Boolean = false,
    @SerializedName("path")
    val remotePath: String = "",
    @SerializedName("media_type")
    val mediaType: String = "",
    @SerializedName("modified")
    val modifiedAt: String = ""
) : Parcelable