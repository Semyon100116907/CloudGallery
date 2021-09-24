package com.semisonfire.cloudgallery.ui.trash.data

import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.api.IMAGE_SIZE_XL
import com.semisonfire.cloudgallery.core.data.remote.api.SORT_DELETED_DESC
import com.semisonfire.cloudgallery.core.logger.printThrowable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

private const val TRASH_PATH = "trash:/"

class TrashRepository @Inject constructor(
    private val diskApi: DiskApi
) {

    fun getTrashPhotos(limit: Int, page: Int): Single<List<Photo>> {
        return diskApi
            .getTrashFiles(
                TRASH_PATH,
                limit,
                limit * (page - 1),
                IMAGE_SIZE_XL,
                SORT_DELETED_DESC
            )
            .map { it.trashResponse?.photos ?: emptyList() }
            .onErrorReturn {
                it.printThrowable()
                emptyList()
            }
    }

    fun restoreTrashPhoto(photo: Photo): Observable<Photo> {
        return diskApi.restorePhoto(photo.remotePath)
            .andThen(Observable.just(photo))
    }

    fun deleteTrashPhoto(photo: Photo): Observable<Photo> {
        return diskApi.deletePermanently(photo.remotePath)
            .andThen(Observable.just(photo))
    }

    fun clearTrash(): Completable {
        return diskApi.deletePermanently(null)
    }

}