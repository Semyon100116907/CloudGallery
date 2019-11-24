package com.semisonfire.cloudgallery.core.presentation

import android.support.annotation.CallSuper
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : MvpView> : MvpPresenter<V> {

  protected val compositeDisposable = CompositeDisposable()
  protected var view: V? = null

  @CallSuper
  override fun attachView(view: V) {
    this.view = view
  }

  override fun detachView() {
    this.view = null
  }

  @CallSuper
  override fun dispose() {
    detachView()
    compositeDisposable.clear()
  }
}