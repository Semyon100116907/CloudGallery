package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.ui.disk.data.DiskMapper
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

const val LIMIT = 15

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
    private val uploadManager: UploadManager,
    private val mapper: DiskMapper
) : DiskPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val viewModel = DiskViewModel()

    private val loadListener = PublishSubject.create<Unit>()

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

        compositeDisposable.add(
            loadListener
                .subscribeOn(background())
                .observeOn(background())
                .concatMapSingle {
                    val page = viewModel.currentPage.getAndIncrement()

                    diskRepository
                        .getPhotos(page, LIMIT)
                        .map { mapper.map(it, page) }
                        .map { itemMap ->
                            val hasMore = itemMap.isNotEmpty()
                            viewModel.hasMore.set(hasMore)

                            if (page == 0) {
                                viewModel.setItems(itemMap)
                                DiskResult.Loaded(
                                    photos = viewModel.getListItems(),
                                    hasMore = hasMore
                                )
                            } else {
                                viewModel.mergeItems(itemMap)
                                DiskResult.LoadMoreCompleted(
                                    photos = viewModel.getListItems(),
                                    hasMore = hasMore
                                )
                            }
                        }
                }
                .subscribe {
                    diskResultListener.onNext(it)
                }
        )
    }

    override fun observeContent(): Observable<DiskViewModel> {
        return contentListener
    }

    override fun observeDiskResult(): Observable<DiskResult> {
        return diskResultListener
    }

    override fun getPhotos() {
        viewModel.currentPage.set(0)
        loadListener.onNext(Unit)
    }

    override fun loadMorePhotos() {
        loadListener.onNext(Unit)
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