package com.semisonfire.cloudgallery.ui.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.net.URL
import javax.inject.Inject

interface PhotoDetailPresenter : Presenter {

    fun download(photo: Photo)
    fun delete(photo: Photo, from: Int)
    fun restore(photo: Photo)
    fun createShareFile(bitmap: Bitmap)

    fun observePhotoDownloaded(): Observable<String>
    fun observeFileChanged(): Observable<Photo>
    fun observeFilePrepared(): Observable<Uri>

}

class PhotoDetailPresenterImpl @Inject constructor(
    private val diskRepository: DiskRepository,
    private val trashRepository: TrashRepository
) : PhotoDetailPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val photoDownloadListener = PublishSubject.create<String>()

    private val filePreparedListener = PublishSubject.create<Uri>()
    private val fileChangedListener = PublishSubject.create<Photo>()

    override fun observePhotoDownloaded(): Observable<String> {
        return photoDownloadListener
    }

    override fun observeFileChanged(): Observable<Photo> {
        return fileChangedListener
    }

    override fun observeFilePrepared(): Observable<Uri> {
        return filePreparedListener
    }

    override fun download(photo: Photo) {
        compositeDisposable.add(
            diskRepository.getDownloadLink(photo)
                .map {
                    val url = URL(it.href)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    FileUtils.savePublicFile(bitmap, photo.name)
                }
                .subscribeOn(background())
                .subscribe(
                    { photoDownloadListener.onNext(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun delete(photo: Photo, from: Int) {
        val delete = when (from) {
            PhotoDetailActivity.FROM_DISK -> diskRepository.deletePhoto(photo)
            PhotoDetailActivity.FROM_TRASH -> trashRepository.deleteTrashPhoto(photo)
            else -> Observable.just(photo)
        }

        compositeDisposable.add(
            delete
                .subscribeOn(background())
                .subscribe(
                    { fileChangedListener.onNext(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun restore(photo: Photo) {
        compositeDisposable.add(
            trashRepository.restoreTrashPhoto(photo)
                .subscribeOn(background())
                .subscribe(
                    { fileChangedListener.onNext(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun createShareFile(bitmap: Bitmap) {
        compositeDisposable.add(
            Single.just(bitmap)
                .map { FileUtils.createShareFile(it) }
                .subscribeOn(background())
                .subscribe(
                    { filePreparedListener.onNext(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun dispose() {
        compositeDisposable.clear()
    }
}