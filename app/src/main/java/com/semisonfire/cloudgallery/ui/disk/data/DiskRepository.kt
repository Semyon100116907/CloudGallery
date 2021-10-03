package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.api.IMAGE_MEDIA_TYPE
import com.semisonfire.cloudgallery.data.remote.api.IMAGE_SIZE_XL
import com.semisonfire.cloudgallery.data.remote.api.LinkResponse
import com.semisonfire.cloudgallery.data.remote.api.SORT_MODIFIED_DESC
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class DiskRepository @Inject constructor(
    private val diskApi: DiskApi
) {

    fun getPhotos(page: Int, limit: Int): Single<List<Photo>> {
        return diskApi
            .getDiskImages(
                limit,
                limit * page,
                IMAGE_MEDIA_TYPE,
                IMAGE_SIZE_XL,
                SORT_MODIFIED_DESC
            )
            .map { it.items ?: emptyList() }
            .onErrorReturn { emptyList() }
    }

    fun getDownloadLink(path: String): Observable<LinkResponse> {
        return diskApi.getDownloadLink(path)
    }

}