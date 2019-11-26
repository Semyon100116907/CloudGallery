package com.semisonfire.cloudgallery.ui.trash.data

import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepository @Inject constructor(
  private val diskApi: DiskApi
) {

  fun getTrashPhotos(limit: Int, page: Int): Single<List<Photo>> {
    return diskApi.getTrashFiles("trash:/", limit, limit * (page - 1), "XL", "-deleted")
      .map { it.trashResponse?.photos ?: emptyList() }
      .onErrorReturn { emptyList() }
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