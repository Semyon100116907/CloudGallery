package com.semisonfire.cloudgallery.ui.main

import android.os.Bundle
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.ContentActivity
import com.semisonfire.cloudgallery.databinding.ActivityMainBinding
import com.semisonfire.cloudgallery.di.provider.provideComponent
import com.semisonfire.cloudgallery.logger.printThrowable
import com.semisonfire.cloudgallery.navigation.ScreenKey
import com.semisonfire.cloudgallery.navigation.destination.Destination
import com.semisonfire.cloudgallery.navigation.router.Router
import com.semisonfire.cloudgallery.ui.main.di.DaggerMainComponent
import com.semisonfire.cloudgallery.ui.main.ui.state.MainStateView
import com.semisonfire.cloudgallery.ui.main.ui.state.StateViewController
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.string
import javax.inject.Inject

class MainActivity : ContentActivity() {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var stateViewController: StateViewController

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerMainComponent
            .factory()
            .create(
                provideComponent()
            )
            .inject(this)

        router.bind(supportFragmentManager)

        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        bind()

        if (intent != null && intent.data != null) {
            login()
        }

        if (savedInstanceState == null) {
            router.replaceScreen(Destination(ScreenKey.DISK))
        }
    }

    fun bind() {
        setSupportActionBar(viewBinding.includeToolbar.toolbar)
        addBottomNavigation()

        stateViewController.bindStateDelegate(findViewById(android.R.id.content))
        stateViewController.updateStateView(MainStateView.CONTENT)
    }

    override fun onStart() {
        super.onStart()

        stateViewController.updateStateView(MainStateView.LOADER)
        disposables.addAll(
            presenter
                .observeAuth()
                .observeOn(foreground())
                .subscribe({
                    val state =
                        if (it.token.isNotEmpty()) MainStateView.CONTENT else MainStateView.AUTH
                    stateViewController.updateStateView(state)
                }, {
                    it.printThrowable()
                })
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    /** Oauth login.  */
    private fun login() {
        val data = intent.data
        if (data != null) {
            presenter.login(data.toString())
        }

        intent = null
    }

    /** Create navigation instance.  */
    private fun addBottomNavigation() {
        viewBinding.navBottom.setOnItemSelectedListener { item ->
            val title: String
            val key = when (item.itemId) {
                R.id.nav_disk -> {
                    title = string(R.string.msg_disk)
                    ScreenKey.DISK
                }
                R.id.nav_trash -> {
                    title = string(R.string.msg_trash)
                    ScreenKey.TRASH_BIN
                }
                R.id.nav_settings -> {
                    title = string(R.string.msg_settings)
                    ScreenKey.SETTINGS
                }
                else -> return@setOnItemSelectedListener false
            }

            viewBinding.includeToolbar.toolbar.title = title
            router.replaceScreen(Destination(key))
            true
        }
    }

    override fun onBackPressed() {
        if (viewBinding.navBottom.selectedItemId == R.id.nav_disk) {
            super.onBackPressed()
        } else {
            viewBinding.navBottom.selectedItemId = R.id.nav_disk
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        router.unbind()
    }
}
