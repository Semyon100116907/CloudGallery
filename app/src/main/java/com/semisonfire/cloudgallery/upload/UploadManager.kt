package com.semisonfire.cloudgallery.upload

import com.semisonfire.cloudgallery.data.local.LocalDatabase
import com.semisonfire.cloudgallery.data.local.entity.PhotoEntity
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManager @Inject constructor(
    private val diskApi: DiskApi,
    private val database: LocalDatabase,
    private val diskRepository: DiskRepository
) {

    //Upload
    private val uploadListener = PublishSubject.create<Photo>()
    private val uploadFailedListener = PublishSubject.create<Photo>()

    private val uploadingQueue: Queue<Photo> = LinkedList()

    fun getUploadingPhotos(): Single<List<Photo>> {
        return database.photoDao.getUploadingPhotos()
            .map {
                it.map { entity ->
                    Photo(
                        id = entity.id,
                        name = entity.name,
                        preview = entity.preview,
                        localPath = entity.localPath,
                        file = entity.file,
                        isUploaded = entity.isUploaded,
                        remotePath = entity.remotePath,
                        modifiedAt = entity.modifiedAt
                    )
                }
            }
    }

    fun uploadPhotos(vararg photos: Photo) {
        uploadingQueue.addAll(photos)

        while (!uploadingQueue.isEmpty()) {
            uploadListener.onNext(uploadingQueue.remove())
        }
    }

    fun uploadListener(): Observable<UploadResult> =
        Observable
            .merge(
                uploadCompleteListener().map { UploadResult.Complete(it) },
                uploadFailedListener.map { UploadResult.Fail(it) }
            )

    private fun uploadCompleteListener(): Observable<Photo> {
        return uploadListener
            .observeOn(Schedulers.io())
            .switchMap { photo ->
                database.photoDao.insert(
                    PhotoEntity(
                        id = photo.id,
                        name = photo.name,
                        preview = photo.preview,
                        localPath = photo.localPath,
                        file = photo.file,
                        remotePath = photo.remotePath,
                        isUploaded = photo.isUploaded,
                        modifiedAt = photo.modifiedAt
                    )
                )

                photo.isUploaded = false
                upload(photo)
            }
    }

    /**
     * Upload one photo at the time.
     */
    private fun upload(photo: Photo): Observable<Photo> {
        return diskApi
            .getUploadLink("disk:/" + photo.name, false)
            .subscribeOn(background())
            .flatMap { diskRepository.savePhoto(photo, it) }
            .retryWhen {
                it.flatMap { throwable ->
                    if (throwable is InternetUnavailableException) {
                        uploadFailedListener.onNext(Photo())
                        return@flatMap Observable.timer(2, TimeUnit.SECONDS)
                    }

                    Observable.error(throwable)
                }
            }
            .filter { it.isUploaded }
            .flatMapSingle {
                database.photoDao.deleteById(it.id)
                Single.just(
                    it.copy(
                        preview = "file://" + File(it.localPath).absolutePath,
                        modifiedAt = DateUtils.currentDate
                    )
                )
            }
    }
}