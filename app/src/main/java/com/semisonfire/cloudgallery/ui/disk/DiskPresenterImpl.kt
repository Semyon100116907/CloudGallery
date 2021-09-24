package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.data.UploadRepository
import com.semisonfire.cloudgallery.ui.disk.model.DiskViewModel
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import com.semisonfire.cloudgallery.utils.foreground
import io.reactivex.Observable
import java.net.URL

const val LIMIT = 15

interface DiskPresenter : MvpPresenter<DiskViewModel, DiskView> {

    fun getPhotos()
    fun uploadPhotos(photos: List<Photo>)
    fun uploadPhoto(photo: Photo)
    fun getUploadingPhotos()
    fun downloadPhotos(photos: List<Photo>)
    fun deletePhotos(photos: List<Photo>)
    fun loadMorePhotos()
}

class DiskPresenterImpl(
    private val diskRepository: DiskRepository,
    private val uploadRepository: UploadRepository
) : BasePresenter<DiskViewModel, DiskView>(), DiskPresenter {

    override val viewModel = DiskViewModel()

    init {
        compositeDisposable.add(
            uploadRepository.uploadListener()
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe({
                    viewModel.photoList.add(it.photo)
                    view?.onPhotoUploaded(it.photo, it.uploaded)
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun getPhotos() {
        val currentPage = viewModel.currentPage
        currentPage.set(0)
        compositeDisposable.add(
            diskRepository
                .getPhotos(currentPage.get(), LIMIT)
                .subscribeOn(background())
                .observeOn(foreground())
                .doOnSuccess { currentPage.getAndIncrement() }
                .subscribe({

                    viewModel.photoList.apply {
                        clear()
                        addAll(it)
                    }

                    view?.onPhotosLoaded(it)
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun loadMorePhotos() {
        val currentPage = viewModel.currentPage
        compositeDisposable.add(
            diskRepository
                .getPhotos(currentPage.incrementAndGet(), LIMIT)
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe({
                    viewModel.photoList.addAll(it)
                    view?.loadMoreComplete(it)
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun getUploadingPhotos() {
        compositeDisposable.add(
            uploadRepository.uploadingPhotos
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe(
                    { view?.onUploadingPhotos(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun downloadPhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { diskRepository.getDownloadLink(it) }
                .filter { !it.href.isNullOrEmpty() }
                .map {
                    val url = URL(it.href)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    FileUtils.savePublicFile(bitmap, it.photo.name)
                }
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe(
                    { view?.onPhotoDownloaded(it) },
                    { it.printThrowable() }
                )
        )
    }

    override fun deletePhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { diskRepository.deletePhoto(it) }
                .subscribeOn(background())
                .observeOn(foreground())
                .subscribe({
                    viewModel.photoList.remove(it)
                    view?.onPhotoDeleted(it)
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun uploadPhoto(photo: Photo) {
        uploadRepository.uploadPhotos(photo)
    }

    override fun uploadPhotos(photos: List<Photo>) {
        uploadRepository.uploadPhotos(*photos.toTypedArray())
    }
}