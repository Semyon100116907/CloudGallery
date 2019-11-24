package com.semisonfire.cloudgallery.core.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.permisson.PermissionManager
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.core.ui.navigation.router.Router
import com.semisonfire.cloudgallery.utils.printThrowable
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>>
  : AppCompatActivity(), MvpView, HasSupportFragmentInjector {

  @Inject
  internal lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

  @Inject
  lateinit var permissionManager: PermissionManager

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var presenter: P

  protected val disposables: CompositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(layout())
    bind()
  }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> {
    return supportFragmentInjector
  }

  @CallSuper
  protected open fun bind() {
  }

  override fun onResume() {
    super.onResume()
    presenter.attachView(getMvpView())
  }

  override fun onPause() {
    super.onPause()
    presenter.detachView()
  }

  protected fun getMvpView(): V {
    return this as V
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionManager.onRequestPermissionsResult(
      this,
      requestCode,
      permissions,
      grantResults
    )
  }

  override fun onError(throwable: Throwable) {
    throwable.printThrowable()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 0) {
      router.back()
    } else {
      super.onBackPressed()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
    presenter.dispose()
  }

  abstract fun layout(): Int
}