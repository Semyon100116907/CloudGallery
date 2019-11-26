package com.semisonfire.cloudgallery.core.mvp

interface MvpPresenter<V : MvpView> {

  fun attachView(view: V)

  fun detachView()

  fun dispose()
}
