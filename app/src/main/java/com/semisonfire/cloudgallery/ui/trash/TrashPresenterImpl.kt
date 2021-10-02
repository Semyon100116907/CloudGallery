package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.common.photo.PhotoItem
import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.logger.printThrowable
import com.semisonfire.cloudgallery.ui.disk.LIMIT
import com.semisonfire.cloudgallery.ui.trash.data.TrashRepository
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

interface TrashPresenter : Presenter {

    fun observeTrashBinResult(): Observable<TrashBinResult>

    fun getPhotos(page: Int)
    fun restorePhotos(photos: List<Photo>)
    fun deletePhotos(photos: List<Photo>)
    fun clear()
}

class TrashBinMapper @Inject constructor() {

    fun map(photos: List<Photo>): List<Item> {
        return photos
            .asSequence()
            .map { PhotoItem(it.id, it.name, it.preview) }
            .toList()
    }
}

class TrashPresenterImpl @Inject constructor(
    private val trashRepository: TrashRepository,
    private val trashBinMapper: TrashBinMapper
) : TrashPresenter {

    private val compositeDisposable = CompositeDisposable()

    private val trashBinResultListener = PublishSubject.create<TrashBinResult>()

    override fun observeTrashBinResult(): Observable<TrashBinResult> {
        return trashBinResultListener
    }

    override fun getPhotos(page: Int) {
        compositeDisposable.add(
            trashRepository.getTrashPhotos(LIMIT, page)
                .map { trashBinMapper.map(it) }
                .subscribeOn(background())
                .subscribe(
                    { trashBinResultListener.onNext(TrashBinResult.Loaded(it)) },
                    { it.printThrowable() }
                )
        )
    }

    override fun restorePhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { trashRepository.restoreTrashPhoto(it) }
                .subscribeOn(background())
                .subscribe(
                    { trashBinResultListener.onNext(TrashBinResult.PhotoRestored(it)) },
                    { it.printThrowable() }
                )
        )
    }

    override fun deletePhotos(photos: List<Photo>) {
        compositeDisposable.add(
            Observable.fromIterable(photos.toMutableList())
                .concatMap { trashRepository.deleteTrashPhoto(it) }
                .subscribeOn(background())
                .subscribe(
                    { trashBinResultListener.onNext(TrashBinResult.PhotoDeleted(it)) },
                    { it.printThrowable() }
                )
        )
    }

    override fun clear() {
        compositeDisposable.add(
            trashRepository.clearTrash()
                .subscribeOn(background())
                .subscribe(
                    { trashBinResultListener.onNext(TrashBinResult.Cleared) },
                    { it.printThrowable() }
                )
        )
    }

    override fun dispose() {
        super.dispose()
        compositeDisposable.clear()
    }
}