package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.data.remote.auth.Auth
import com.semisonfire.cloudgallery.core.data.remote.auth.AuthRepository
import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.ui.main.model.MainViewModel
import io.reactivex.Observable

interface MainPresenter : MvpPresenter<MainViewModel, MainView> {
    fun saveToken(token: String)
    fun getTokenListener(): Observable<Auth.AuthModel>
}

class MainPresenterImpl(
    private val authRepository: AuthRepository
) : BasePresenter<MainViewModel, MainView>(),
    MainPresenter {

    override val viewModel = MainViewModel()

    override fun saveToken(token: String) {
        authRepository.saveToken(token)
    }

    override fun getTokenListener(): Observable<Auth.AuthModel> {
        return authRepository.getAuthObservable().ofType(Auth.AuthModel::class.java)
    }
}
