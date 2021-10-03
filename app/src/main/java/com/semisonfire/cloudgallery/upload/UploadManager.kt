package com.semisonfire.cloudgallery.upload

import com.semisonfire.cloudgallery.data.local.LocalDatabase
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.model.remote.Link
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.Single
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
    private val uploadSubject = PublishSubject.create<Photo>()
    private val uploadSubjectInformer = PublishSubject.create<Photo>()
    private val uploadingPhotosQueue: Queue<Photo> = LinkedList()

    val uploadingPhotos: Single<List<Photo>>
        get() = database.photoDao.uploadingPhotos

    fun uploadPhotos(vararg photos: Photo) {
        uploadingPhotosQueue.addAll(photos)

        while (!uploadingPhotosQueue.isEmpty()) {
            uploadSubject.onNext(uploadingPhotosQueue.remove())
        }
    }

    fun uploadListener(): Observable<UploadResult> =
        Observable
            .merge(
                uploadCompleteListener().map { UploadResult.Complete(it) },
                uploadFailListener().map { UploadResult.Fail(it) }
            )

    private fun uploadCompleteListener() = uploadSubject.hide().switchMap { uploadPhoto(it) }
    private fun uploadFailListener() = uploadSubjectInformer.hide()

    private fun getUploadLink(photo: Photo): Observable<Link> {
        return diskApi.getUploadLink("disk:/" + photo.name, false)
            .doOnNext { it.photo = photo }
    }

    private fun uploadPhoto(photo: Photo): Observable<Photo> {
        return saveUploadingPhoto(photo)
            .flatMapObservable { upload(it) }
    }

    private fun saveUploadingPhoto(photo: Photo): Single<Photo> {
        return Single
            .fromCallable {
                database.photoDao.insertPhoto(photo)
                photo.isUploaded = false
                photo
            }
            .subscribeOn(background())
    }

    private fun removeUploadingPhoto(photo: Photo): Single<Photo> {
        return Single.fromCallable {
            database.photoDao.deletePhoto(photo)
            photo
        }
    }

    /**
     * Upload one photo at the time.
     */
    private fun upload(photo: Photo): Observable<Photo> {
        return Observable
            .just(photo)
            .flatMap {
                getUploadLink(it)
                    .flatMap { diskRepository.savePhoto(it.photo, it) }
            }
            .retryWhen {
                it.flatMap { throwable ->
                    if (throwable is InternetUnavailableException) {
                        uploadSubjectInformer.onNext(Photo())
                        return@flatMap Observable.timer(2, TimeUnit.SECONDS)
                    }

                    Observable.error(throwable)
                }
            }
            .filter { it.isUploaded }
            .flatMapSingle {
                val absolutePath = "file://" + File(it.localPath).absolutePath
                removeUploadingPhoto(
                    it.copy(
                        preview = absolutePath,
                        modifiedAt = DateUtils.currentDate
                    )
                )
            }
    }
}