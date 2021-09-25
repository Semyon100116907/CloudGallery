package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.api.IMAGE_MEDIA_TYPE
import com.semisonfire.cloudgallery.core.data.remote.api.IMAGE_SIZE_XL
import com.semisonfire.cloudgallery.core.data.remote.api.SORT_MODIFIED_DESC
import com.semisonfire.cloudgallery.ui.disk.model.remote.Link
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DiskRepository @Inject constructor(
    private val diskApi: DiskApi,
    private val mapper: DiskMapper
) {

    fun getPhotos(page: Int, limit: Int): Single<List<Item>> {
        return diskApi
            .getDiskImages(
                limit,
                limit * page,
                IMAGE_MEDIA_TYPE,
                IMAGE_SIZE_XL,
                SORT_MODIFIED_DESC
            )
            .map { response ->
                response.items?.let { mapper.map(it) } ?: emptyList()
            }
            .onErrorReturn { emptyList() }
    }

    fun savePhoto(photo: Photo, link: Link): Observable<Photo> {
        val file = File(photo.localPath)
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            reqFile
        )
        return diskApi.uploadImage(link.href, body)
            .andThen(
                Observable
                    .just(photo)
                    .doOnNext {
                        it.isUploaded = true
                        it.remotePath = "disk:/" + it.name
                    }
            )
    }

    fun deletePhoto(photo: Photo): Observable<Photo> {
        return diskApi.deleteImage(photo.remotePath)
            .andThen(Observable.just(photo))
    }

    fun getDownloadLink(photo: Photo): Observable<Link> {
        return diskApi.getDownloadLink(photo.remotePath).doOnNext { it.photo = photo }
    }

}