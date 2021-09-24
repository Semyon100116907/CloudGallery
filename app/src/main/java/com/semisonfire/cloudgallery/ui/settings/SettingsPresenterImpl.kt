package com.semisonfire.cloudgallery.ui.settings

import com.semisonfire.cloudgallery.core.mvp.MvpPresenter
import com.semisonfire.cloudgallery.core.presentation.BasePresenter
import com.semisonfire.cloudgallery.ui.settings.model.SettingsViewModel

interface SettingsPresenter : MvpPresenter<SettingsViewModel, SettingsView>

class SettingsPresenterImpl : BasePresenter<SettingsViewModel, SettingsView>(), SettingsPresenter {
    override val viewModel = SettingsViewModel()
}