package com.semisonfire.cloudgallery.ui.detail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.logger.printThrowable
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
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
    fun createShareFile(bitmap: Bitmap)

    fun observePhotoDownloaded(): Observable<String>
    fun observeFilePrepared(): Observable<Uri>
}

class PhotoDetailPresenterImpl @Inject constructor(
    private val diskRepository: DiskRepository
) : PhotoDetailPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val photoDownloadListener = PublishSubject.create<String>()
    private val filePreparedListener = PublishSubject.create<Uri>()

    override fun observePhotoDownloaded(): Observable<String> {
        return photoDownloadListener
    }

    override fun observeFilePrepared(): Observable<Uri> {
        return filePreparedListener
    }

    override fun download(photo: Photo) {
        compositeDisposable.add(
            diskRepository
                .getDownloadLink(photo.remotePath)
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