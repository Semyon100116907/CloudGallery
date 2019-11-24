package com.semisonfire.cloudgallery.ui.main

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.mvp.MvpView

interface MainContract {
  interface View : MvpView
  interface Presenter : MvpPresenter<View>
}
