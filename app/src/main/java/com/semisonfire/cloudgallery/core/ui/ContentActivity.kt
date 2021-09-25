package com.semisonfire.cloudgallery.core.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
abstract class ContentActivity : AppCompatActivity() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout())
        bind()
    }

    @CallSuper
    protected open fun bind() {
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    abstract fun layout(): Int
}