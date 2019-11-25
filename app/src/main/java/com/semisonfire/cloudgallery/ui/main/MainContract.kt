package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.data.remote.auth.Auth
import io.reactivex.Observable

interface MainContract {
  interface View : MvpView
  interface Presenter : MvpPresenter<View> {
    fun saveToken(token: String)
    fun getTokenListener(): Observable<Auth.AuthModel>
  }
}
