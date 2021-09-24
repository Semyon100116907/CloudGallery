package com.semisonfire.cloudgallery.core.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.mvp.MvpViewModel
import com.semisonfire.cloudgallery.core.permisson.PermissionManager
import com.semisonfire.cloudgallery.core.ui.navigation.router.Router
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<M : MvpViewModel, V : MvpView<M>, P : MvpPresenter<M, V>>
    : AppCompatActivity(), MvpView<M> {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var presenter: P

    protected val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout())
        bind()
    }

    @CallSuper
    protected open fun bind() {
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(getMvpView())
    }

    override fun showContent(model: M) {

    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    private fun getMvpView(): V {
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