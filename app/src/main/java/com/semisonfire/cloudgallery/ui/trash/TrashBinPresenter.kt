package com.semisonfire.cloudgallery.ui.trash

import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.logger.printThrowable
import com.semisonfire.cloudgallery.ui.disk.LIMIT
import com.semisonfire.cloudgallery.ui.trash.data.TrashBinMapper
import com.semisonfire.cloudgallery.ui.trash.data.TrashBinRepository
import com.semisonfire.cloudgallery.ui.trash.model.TrashBinResult
import com.semisonfire.cloudgallery.utils.background
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

interface TrashBinPresenter : Presenter {

    fun observeTrashBinResult(): Observable<TrashBinResult>

    fun getPhotos(page: Int)
    fun clear()
}

class TrashBinPresenterImpl @Inject constructor(
    private val trashRepository: TrashBinRepository,
    private val trashBinMapper: TrashBinMapper
) : TrashBinPresenter {

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