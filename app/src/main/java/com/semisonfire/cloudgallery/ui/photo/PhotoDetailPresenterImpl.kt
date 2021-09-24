package com.semisonfire.cloudgallery.ui.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.photo.model.PhotoDetailViewModel
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import io.reactivex.Observable
import io.reactivex.Single
import java.net.URL

interface PhotoDetailPresenter : MvpPresenter<PhotoDetailViewModel, PhotoDetailView> {

    fun download(photo: Photo)
    fun delete(photo: Photo, from: Int)
    fun restore(photo: Photo)
    fun createShareFile(bitmap: Bitmap)
}

class PhotoDetailPresenterImpl(
    private val diskRepository: DiskRepository,
    private val trashRepository: TrashRepository
) : BasePresenter<PhotoDetailViewModel, PhotoDetailView>(), PhotoDetailPresenter {

    override val viewModel = PhotoDetailViewModel()

    override fun download(photo: Photo) {
        compositeDisposable.add(
            diskRepository.getDownloadLink(photo)
                .map {
                    val url = URL(it.href)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    FileUtils.savePublicFile(bitmap, photo.name)
                }
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe(
                    { view?.onPhotoDownloaded(it) },
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
                .observeOn(foreground())
                .subscribe(
                    { view?.onFilesChanged(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun restore(photo: Photo) {
        compositeDisposable.add(
            trashRepository.restoreTrashPhoto(photo)
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe(
                    { view?.onFilesChanged(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun createShareFile(bitmap: Bitmap) {
        compositeDisposable.add(
            Single.just(bitmap)
                .map { FileUtils.createShareFile(it) }
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe(
                    { view?.onFilePrepared(it) },
                    { it.printThrowable() }
                )
        )
    }

}