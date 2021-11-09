package com.semisonfire.cloudgallery.ui.disk

import android.graphics.BitmapFactory
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.logger.printThrowable
import com.semisonfire.cloudgallery.ui.disk.data.DiskMapper
import com.semisonfire.cloudgallery.ui.disk.data.DiskRepository
import com.semisonfire.cloudgallery.ui.disk.model.DiskState
import com.semisonfire.cloudgallery.upload.UploadManager
import com.semisonfire.cloudgallery.upload.adapter.UploadItem
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
    fun loadMorePhotos()

    fun observeContent(): Observable<DiskState>
    fun observeDiskResult(): Observable<DiskResult>
}

class DiskPresenterImpl @Inject constructor(
    private val diskRepository: DiskRepository,
    private val uploadManager: UploadManager,
    private val mapper: DiskMapper
) : DiskPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val loadListener = PublishSubject.create<Unit>()
    private val diskResultListener = PublishSubject.create<DiskResult>()

    private val stateListener = BehaviorSubject.createDefault(DiskState())
    private val state
        get() = stateListener.value!!

    init {
        compositeDisposable.add(
            uploadManager
                .uploadListener()
                .observeOn(background())
                .filter { it.uploaded }
                .subscribe({ uploadResult ->
                    val uploading = state.uploading
                    val uploadedPhoto = uploadResult.photo
                    if (uploading != null) {
                        uploading.items as MutableList<UploadItem>
                        uploading.items.removeAll {
                            it.id == uploadedPhoto.id
                        }

                        if (uploading.items.isEmpty()) {
                            state.uploading = null
                        }

                        val itemsMap = mapper.map(listOf(uploadedPhoto), -1)

                        state.mergeItems(itemsMap)

                        diskResultListener.onNext(DiskResult.Update(state.getListItems()))
                    }
                }, {
                    it.printThrowable()
                })
        )

        compositeDisposable.add(
            loadListener
                .observeOn(background())
                .concatMapSingle {
                    val page = state.currentPage.getAndIncrement()

                    diskRepository
                        .getPhotos(page, LIMIT)
                        .map {
                            val itemMap = mapper.map(it, page)
                            val hasMore = itemMap.isNotEmpty()
                            state.hasMore.set(hasMore)

                            if (page == 0) {
                                state.setItems(itemMap)
                                DiskResult.Loaded(
                                    photos = state.getListItems(),
                                    hasMore = hasMore
                                )
                            } else {
                                state.mergeItems(itemMap)
                                DiskResult.LoadMoreCompleted(
                                    photos = state.getListItems(),
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

    override fun observeContent(): Observable<DiskState> {
        return stateListener
    }

    override fun observeDiskResult(): Observable<DiskResult> {
        return diskResultListener
    }

    override fun getPhotos() {
        state.currentPage.set(0)
        state.hasMore.set(true)

        loadListener.onNext(Unit)
    }

    override fun loadMorePhotos() {
        loadListener.onNext(Unit)
    }

    override fun getUploadingPhotos() {
        compositeDisposable.add(
            uploadManager.getUploadingPhotos()
                .subscribeOn(background())
                .map { mapper.mapUploading(it) }
                .subscribe(
                    {
                        state.uploading = it
                        diskResultListener.onNext(DiskResult.Uploading(it))
                    },
                    { it.printThrowable() }
                )
        )
    }

    override fun downloadPhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { photo ->
                    diskRepository
                        .getDownloadLink(photo.remotePath)
                        .filter { !it.href.isNullOrEmpty() }
                        .map {
                            val url = URL(it.href)
                            val bitmap =
                                BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            FileUtils.savePublicFile(bitmap, photo.name)
                        }
                        .subscribeOn(background())
                }
                .subscribe(
                    {
                        diskResultListener.onNext(DiskResult.PhotoDownloaded(it))
                    },
                    { it.printThrowable() }
                )
        )
    }

    override fun uploadPhoto(photo: Photo) {
        createFakeUploads(listOf(photo))

        uploadManager.uploadPhotos(photo)
    }

    override fun uploadPhotos(photos: List<Photo>) {
        createFakeUploads(photos)

        uploadManager.uploadPhotos(*photos.toTypedArray())
    }

    private fun createFakeUploads(photos: List<Photo>) {
        var uploading = state.uploading
        if (uploading == null) {
            uploading = mapper.mapUploading(photos)
            state.uploading = uploading

        } else {
            val items = uploading.items
            items as MutableList<UploadItem>
            items.addAll(mapper.mapUploadItems(photos))
        }

        diskResultListener.onNext(DiskResult.Uploading(uploading))
    }

    override fun dispose() {
        super.dispose()
        compositeDisposable.clear()
    }
}