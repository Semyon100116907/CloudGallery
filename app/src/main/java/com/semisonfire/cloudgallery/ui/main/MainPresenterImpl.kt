package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.ui.Presenter
import com.semisonfire.cloudgallery.data.remote.auth.AuthManager
import com.semisonfire.cloudgallery.data.remote.auth.AuthModel
import io.reactivex.Observable
import java.util.regex.Pattern
import javax.inject.Inject

interface MainPresenter : Presenter {
    fun login(url: String)
    fun observeAuth(): Observable<AuthModel>
}

class MainPresenterImpl @Inject constructor(
    private val authManager: AuthManager
) : MainPresenter {

    companion object {
        private val tokenPattern = Pattern.compile("access_token=(.*?)(&|$)")
    }

    override fun login(url: String) {
        val matcher = tokenPattern.matcher(url)
        if (matcher.find()) {
            val token = matcher.group(1)
            if (!token.isNullOrEmpty()) {
                authManager.login(token)
            }
        }
    }

    override fun observeAuth(): Observable<AuthModel> {
        return authManager.observeAuth()
    }
}
