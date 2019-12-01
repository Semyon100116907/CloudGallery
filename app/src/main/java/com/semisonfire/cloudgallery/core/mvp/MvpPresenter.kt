package com.semisonfire.cloudgallery.core.mvp

interface MvpPresenter<M : MvpViewModel, V : MvpView<M>> {

  fun attachView(view: V)

  fun detachView()

  fun dispose()
}
