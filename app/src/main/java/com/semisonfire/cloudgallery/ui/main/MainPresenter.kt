package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.data.remote.auth.Auth
import com.semisonfire.cloudgallery.data.remote.auth.AuthRepository
import io.reactivex.Observable

class MainPresenter(
  private val authRepository: AuthRepository
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

  override fun saveToken(token: String) {
    authRepository.saveToken(token)
  }

  override fun getTokenListener(): Observable<Auth.AuthModel> {
    return authRepository.getAuthObservable().ofType(Auth.AuthModel::class.java)
  }
}
