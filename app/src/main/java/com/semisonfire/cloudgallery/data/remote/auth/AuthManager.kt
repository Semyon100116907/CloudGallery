package com.semisonfire.cloudgallery.data.remote.auth

import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

data class AuthModel(
    val token: String
)

@Singleton
class AuthManager @Inject constructor(
    private val preferences: SharedPreferences
) {

    companion object {
        private const val PREF_TOKEN = "TOKEN"
    }

    private val authListener =
        BehaviorSubject.createDefault(AuthModel(preferences.getString(PREF_TOKEN, null) ?: ""))

    val authModel: AuthModel
        get() = authListener.value!!

    fun login(token: String) {
        preferences.edit()
            .putString(PREF_TOKEN, token)
            .apply()

        authListener.onNext(AuthModel(token))
    }

    fun logout() {
        preferences.edit()
            .remove(PREF_TOKEN)
            .apply()
        authListener.onNext(AuthModel(""))
    }

    fun observeAuth(): Observable<AuthModel> = authListener.hide()
}