package com.semisonfire.cloudgallery.core.presentation

import android.support.annotation.CallSuper
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.mvp.MvpViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<M : MvpViewModel, V : MvpView<M>> : MvpPresenter<V> {

  protected val compositeDisposable = CompositeDisposable()
  protected var view: V? = null

  protected abstract val viewModel: M

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