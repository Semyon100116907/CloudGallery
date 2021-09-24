package com.semisonfire.cloudgallery.ui.main

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.logger.printThrowable
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.ui.BaseActivity
import com.semisonfire.cloudgallery.ui.disk.DISK_KEY
import com.semisonfire.cloudgallery.ui.main.model.MainViewModel
import com.semisonfire.cloudgallery.ui.main.ui.state.MainStateView
import com.semisonfire.cloudgallery.ui.main.ui.state.StateViewController
import com.semisonfire.cloudgallery.ui.settings.SETTINGS_KEY
import com.semisonfire.cloudgallery.ui.trash.TRASH_KEY
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.string
import java.util.regex.Pattern
import javax.inject.Inject

interface MainView : MvpView<MainViewModel>

class MainActivity : BaseActivity<MainViewModel, MainView, MainPresenter>(), MainView {

    @Inject
    lateinit var stateViewController: StateViewController

    private var toolbar: Toolbar? = null
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null && intent.data != null) {
            login()
        }

        val currentScreenKey = if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_CURRENT_SCREEN) ?: ""
        } else {
            DISK_KEY
        }

        router.replaceScreen(currentScreenKey)
    }

    public override fun bind() {
        super.bind()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.nav_bottom)
        addBottomNavigation()

        stateViewController.bindStateDelegate(findViewById(android.R.id.content))
        stateViewController.updateStateView(MainStateView.CONTENT)
    }

    override fun onResume() {
        super.onResume()

        stateViewController.updateStateView(MainStateView.LOADER)
        disposables.addAll(
            presenter
                .getTokenListener()
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

    /** Oauth login.  */
    private fun login() {
        val data = intent.data
        intent = null
        data?.let {
            val pattern = Pattern.compile("access_token=(.*?)(&|$)")
            val matcher = pattern.matcher(data.toString())
            if (matcher.find()) {
                val token = matcher.group(1)
                if (!token.isNullOrEmpty()) {
                    presenter.saveToken(token)
                }
            }
        }
    }

    /** Create navigation instance.  */
    private fun addBottomNavigation() {
        bottomNavigationView?.setOnItemSelectedListener { item ->
            val title: String
            val key = when (item.itemId) {
                R.id.nav_disk -> {
                    title = string(R.string.msg_disk)
                    DISK_KEY
                }
                R.id.nav_trash -> {
                    title = string(R.string.msg_trash)
                    TRASH_KEY
                }
                R.id.nav_settings -> {
                    title = string(R.string.msg_settings)
                    SETTINGS_KEY
                }
                else -> return@setOnItemSelectedListener false
            }

            key.let {
                toolbar?.title = title
                router.replaceScreen(key)
            }
            true
        }
//    bottomNavigationView?.setOnNavigationItemReselectedListener { item ->
//      when (item.itemId) {
//        R.id.nav_disk, R.id.nav_trash -> fragment?.scrollToTop()
//        R.id.nav_settings -> {
//        }
//      }
//    }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_CURRENT_SCREEN, router.getCurrentScreenKey())
    }

    override fun onBackPressed() {
        if (bottomNavigationView?.selectedItemId == R.id.nav_disk) {
            super.onBackPressed()
        } else {
            bottomNavigationView?.selectedItemId = R.id.nav_disk
        }
    }

    override fun layout(): Int {
        return R.layout.activity_main
    }

    companion object {
        //STATE
        private const val STATE_CURRENT_SCREEN = "STATE_CURRENT_SCREEN"
    }
}
