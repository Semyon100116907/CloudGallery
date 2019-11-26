package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.remote.auth.Auth
import com.semisonfire.cloudgallery.data.remote.auth.AuthRepository
import io.reactivex.Observable

interface MainPresenter : MvpPresenter<MainView> {
  fun saveToken(token: String)
  fun getTokenListener(): Observable<Auth.AuthModel>
}

class MainPresenterImpl(
  private val authRepository: AuthRepository
) : BasePresenter<MainView>(),
  MainPresenter {

  override fun saveToken(token: String) {
    authRepository.saveToken(token)
  }

  override fun getTokenListener(): Observable<Auth.AuthModel> {
    return authRepository.getAuthObservable().ofType(Auth.AuthModel::class.java)
  }
}
