package com.semisonfire.cloudgallery.ui.settings

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter

interface SettingsPresenter : MvpPresenter<SettingsView>

class SettingsPresenterImpl : BasePresenter<SettingsView>(), SettingsPresenter