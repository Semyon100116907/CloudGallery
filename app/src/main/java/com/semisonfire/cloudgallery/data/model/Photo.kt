package com.semisonfire.cloudgallery.data.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Photo(
    @PrimaryKey
    @SerializedName("resource_id")
    val id: String = "",
    val name: String = "",
    val preview: String = "",
    val localPath: String = "",
    val file: String = "",
    var isUploaded: Boolean = false,
    @SerializedName("path")
    val remotePath: String = "",
    @SerializedName("modified")
    val modifiedAt: String = ""
) : Parcelable