package com.semisonfire.cloudgallery.core.ui

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

abstract class ContentActivity : AppCompatActivity() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}