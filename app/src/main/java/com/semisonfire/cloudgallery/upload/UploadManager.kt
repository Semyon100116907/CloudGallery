package com.semisonfire.cloudgallery.upload

import com.semisonfire.cloudgallery.data.local.LocalDatabase
import com.semisonfire.cloudgallery.data.local.entity.PhotoEntity
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.data.remote.api.DiskApi
import com.semisonfire.cloudgallery.data.remote.api.LinkResponse
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class UploadManager @Inject constructor(
    private val diskApi: DiskApi,
    private val database: LocalDatabase
) {

    //Upload
    private val uploadListener = PublishSubject.create<Photo>()

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

    fun uploadListener(): Observable<UploadResult> {
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
    private fun upload(photo: Photo): Observable<UploadResult> {
        return diskApi
            .getUploadLink("disk:/" + photo.name, false)
            .subscribeOn(background())
            .flatMapSingle { upload(photo, it) }
            .filter { it.isUploaded }
            .map {
                database.photoDao.deleteById(it.id)

                UploadResult.Success(
                    it.copy(
                        preview = "file://" + File(it.localPath).absolutePath,
                        modifiedAt = DateUtils.currentDate
                    )
                )
            }
    }

    private fun upload(photo: Photo, link: LinkResponse): Single<Photo> {
        val file = File(photo.localPath)
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, reqFile)
        return diskApi
            .uploadImage(link.href, body)
            .toSingle {
                photo.copy(remotePath = "disk:/" + photo.name, isUploaded = true)
            }
    }
}