package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.data.UploadManager
import com.semisonfire.cloudgallery.ui.disk.model.DiskViewModel
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.net.URL
import javax.inject.Inject

const val LIMIT = 20

interface DiskPresenter : Presenter {

    fun getPhotos()
    fun uploadPhotos(photos: List<Photo>)
    fun uploadPhoto(photo: Photo)
    fun getUploadingPhotos()
    fun downloadPhotos(photos: List<Photo>)
    fun deletePhotos(photos: List<Photo>)
    fun loadMorePhotos()

    fun observeContent(): Observable<DiskViewModel>
    fun observeDiskResult(): Observable<DiskResult>
}

class DiskPresenterImpl @Inject constructor(
    private val diskRepository: DiskRepository,
    private val uploadManager: UploadManager
) : DiskPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val viewModel = DiskViewModel()

    private val contentListener = BehaviorSubject.createDefault(viewModel)
    private val diskResultListener = PublishSubject.create<DiskResult>()

    init {
        compositeDisposable.add(
            uploadManager.uploadListener()
                .subscribeOn(background())
                .subscribe({
//                    viewModel.photoList.add(it.photo)
                    diskResultListener.onNext(DiskResult.PhotoUploaded(it.photo, it.uploaded))
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun observeContent(): Observable<DiskViewModel> {
        return contentListener
    }

    override fun observeDiskResult(): Observable<DiskResult> {
        return diskResultListener
    }

    override fun getPhotos() {
        val currentPage = viewModel.currentPage
        currentPage.set(0)
        compositeDisposable.add(
            diskRepository
                .getPhotos(currentPage.get(), LIMIT)
                .subscribeOn(background())
                .doOnSuccess { currentPage.getAndIncrement() }
                .subscribe({

                    viewModel.photoList.apply {
                        clear()
                        addAll(it)
                    }

                    diskResultListener.onNext(DiskResult.Loaded(it))
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
                .subscribe({
                    viewModel.photoList.addAll(it)
                    diskResultListener.onNext(DiskResult.LoadMoreCompleted(it))
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun getUploadingPhotos() {
        compositeDisposable.add(
            uploadManager.uploadingPhotos
                .subscribeOn(background())
                .subscribe(
                    {
                        diskResultListener.onNext(DiskResult.Uploading(it))
                    },
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
                .subscribe(
                    {
                        diskResultListener.onNext(DiskResult.PhotoDownloaded(it))
                    },
                    { it.printThrowable() }
                )
        )
    }

    override fun deletePhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { diskRepository.deletePhoto(it) }
                .subscribeOn(background())
                .subscribe({
//                    viewModel.photoList.remove(it)
                    diskResultListener.onNext(DiskResult.PhotoDeleted(it))
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun uploadPhoto(photo: Photo) {
        uploadManager.uploadPhotos(photo)
    }

    override fun uploadPhotos(photos: List<Photo>) {
        uploadManager.uploadPhotos(*photos.toTypedArray())
    }
}