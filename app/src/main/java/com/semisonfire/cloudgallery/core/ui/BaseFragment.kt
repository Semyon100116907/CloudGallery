package com.semisonfire.cloudgallery.core.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.permisson.PermissionManager
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.core.ui.navigation.router.Router
import com.semisonfire.cloudgallery.utils.printThrowable
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseFragment<V : MvpView, P : MvpPresenter<V>>
  : Fragment(), MvpView {

  @Inject
  lateinit var permissionManager: PermissionManager

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var presenter: P

  protected val disposables: CompositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidSupportInjection.inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val inflatedView = inflater.inflate(layout(), container, false)
    return inflatedView ?: super.onCreateView(inflater, container, savedInstanceState)
  }

  abstract fun layout(): Int

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bind(view)
  }

  @CallSuper
  protected open fun bind(view: View) {

  }

  protected fun updateToolbarTitle(title: String) {
    val activity = activity
    if (activity is AppCompatActivity) {
      activity.supportActionBar?.title = title
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    activity?.let {
      permissionManager.onRequestPermissionsResult(
        it,
        requestCode,
        permissions,
        grantResults
      )
    }
  }

  override fun onError(throwable: Throwable) {
    throwable.printThrowable()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    disposables.dispose()
    presenter.dispose()
  }
}