package com.semisonfire.cloudgallery.ui.trash.data

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.api.IMAGE_SIZE_XL
import com.semisonfire.cloudgallery.data.remote.api.SORT_DELETED_DESC
import com.semisonfire.cloudgallery.logger.printThrowable
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class TrashBinRepository @Inject constructor(
    private val diskApi: DiskApi
) {
    companion object {
        private const val TRASH_PATH = "trash:/"
    }

    fun getTrashPhotos(limit: Int, page: Int): Single<List<Photo>> {
        return diskApi
            .getTrashFiles(
                TRASH_PATH,
                limit,
                limit * (page - 1),
                IMAGE_SIZE_XL,
                SORT_DELETED_DESC
            )
            .map { it.trashResponse?.items ?: emptyList() }
            .onErrorReturn {
                it.printThrowable()
                emptyList()
            }
    }

    fun clearTrash(): Completable {
        return diskApi.deletePermanently(null)
    }
}