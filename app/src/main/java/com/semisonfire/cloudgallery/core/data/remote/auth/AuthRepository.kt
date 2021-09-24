package com.semisonfire.cloudgallery.core.data.remote.auth

import com.semisonfire.cloudgallery.core.data.local.prefs.DiskPreferences
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

typealias Token = String

sealed class Auth {
    data class AuthModel(val token: Token) : Auth()
    object Clear : Auth()
}

@Singleton
class AuthRepository @Inject constructor(
    private val preferences: DiskPreferences
) {

    private val authListener = BehaviorSubject.create<Auth>()

    var authModel: Auth
        private set

    init {

        val token = preferences.prefToken ?: ""
        authModel = Auth.AuthModel(token)
        authListener.onNext(authModel)
    }

    fun getAuthObservable(): Observable<Auth> {
        return authListener.hide().filter { it !is Auth.Clear }
    }

    fun saveToken(token: String) {
        preferences.prefToken = token
        authModel = Auth.AuthModel(token)
        authListener.onNext(authModel)
    }

    fun clear() {
        preferences.clear()

        authModel = Auth.Clear
        authListener.onNext(authModel)
    }
}