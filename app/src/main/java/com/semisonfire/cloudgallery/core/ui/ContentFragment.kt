package com.semisonfire.cloudgallery.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
abstract class ContentFragment : Fragment() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout(), container, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }
}