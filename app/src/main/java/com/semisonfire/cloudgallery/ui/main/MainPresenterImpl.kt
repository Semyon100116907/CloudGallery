package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.data.remote.auth.Auth
import com.semisonfire.cloudgallery.data.remote.auth.AuthManager
import io.reactivex.Observable
import javax.inject.Inject

interface MainPresenter : Presenter {
    fun saveToken(token: String)
    fun observeAuth(): Observable<Auth.AuthModel>
}

class MainPresenterImpl @Inject constructor(
    private val authManager: AuthManager
) : MainPresenter {

    override fun saveToken(token: String) {
        authManager.saveToken(token)
    }

    override fun observeAuth(): Observable<Auth.AuthModel> {
        return authManager.getAuthObservable().ofType(Auth.AuthModel::class.java)
    }
}
