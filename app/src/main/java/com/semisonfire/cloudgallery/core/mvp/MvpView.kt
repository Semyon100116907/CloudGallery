package com.semisonfire.cloudgallery.core.mvp

interface MvpView<M : MvpViewModel> {
    fun showContent(model: M)
}
