package com.semisonfire.cloudgallery.core.mvp

interface MvpPresenter<M : MvpViewModel, V : MvpView<M>> : Presenter {

    fun attachView(view: V)

    fun detachView()
}
