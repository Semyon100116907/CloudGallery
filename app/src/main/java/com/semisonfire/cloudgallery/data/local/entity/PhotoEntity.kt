package com.semisonfire.cloudgallery.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class PhotoEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val preview: String = "",
    @ColumnInfo(name = "local_path")
    val localPath: String = "",
    val file: String = "",
    @ColumnInfo(name = "remote_path")
    val remotePath: String = "",
    @ColumnInfo(name = "upload")
    val isUploaded: Boolean = false,
    @ColumnInfo(name = "modified_at")
    val modifiedAt: String = ""
)