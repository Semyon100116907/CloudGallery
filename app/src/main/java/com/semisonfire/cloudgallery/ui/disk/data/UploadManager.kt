package com.semisonfire.cloudgallery.ui.disk.data

import com.semisonfire.cloudgallery.core.data.local.LocalDatabase
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.core.data.remote.exceptions.InternetUnavailableException
import com.semisonfire.cloudgallery.ui.disk.model.remote.Link
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class UploadResult(open val photo: Photo, val uploaded: Boolean) {
    data class Complete(override val photo: Photo) : UploadResult(photo, true)
    data class Fail(override val photo: Photo) : UploadResult(photo, false)
}

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

    fun uploadListener() =
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
        return Observable.just(photo)
            .flatMap { getUploadLink(it) }
            .flatMap { diskRepository.savePhoto(it.photo, it) }
            .retryWhen {
                it.flatMap { throwable ->
                    if (throwable is InternetUnavailableException) {
                        uploadSubjectInformer.onNext(Photo())
                        return@flatMap Observable.timer(2, TimeUnit.SECONDS)
                    }
                    if (throwable is HttpException) {
                        if (throwable.code() == 409) {
                            photo.name = rename(photo.name)
                            return@flatMap Observable.timer(1, TimeUnit.SECONDS)
                        }
                    }
                    Observable.error<Long>(throwable)
                }
            }
            .filter { it.isUploaded }
            .flatMapSingle {
                val absolutePath = "file://" + File(it.localPath).absolutePath
                it.preview = absolutePath
                it.modifiedAt = DateUtils.currentDate
                removeUploadingPhoto(it)
            }
    }

    /**
     * Rename duplicate file.
     */
    private fun rename(fullName: String): String {
        val split = fullName.split('.')
        val name = split[0]
        val extension = split[1]

        val first = name.indexOf("(") + 1
        val last = name.lastIndexOf(")")

        var newName = if (last != -1) {
            var counter = name.substring(first, last).toInt()
            counter++
            name.substring(0, first - 1) + "(" + counter.toString() + ")"
        } else {
            "$name(1)"
        }
        newName += extension
        return newName
    }
}