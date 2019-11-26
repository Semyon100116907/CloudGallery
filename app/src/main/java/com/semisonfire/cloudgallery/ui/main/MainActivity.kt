package com.semisonfire.cloudgallery.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.di.module.data.remote.OAUTH_URL
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.ui.BaseActivity
import com.semisonfire.cloudgallery.core.ui.state.State
import com.semisonfire.cloudgallery.core.ui.state.StateViewDelegate
import com.semisonfire.cloudgallery.core.ui.state.strategy.EnterActionStrategy
import com.semisonfire.cloudgallery.ui.disk.DISK_KEY
import com.semisonfire.cloudgallery.ui.settings.SETTINGS_KEY
import com.semisonfire.cloudgallery.ui.trash.TRASH_KEY
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.printThrowable
import com.semisonfire.cloudgallery.utils.string
import java.util.regex.Pattern

enum class MainStateView {
  AUTH,
  LOADER,
  CONTENT,
  EMPTY
}

interface MainView : MvpView

class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

  private var toolbar: Toolbar? = null
  private var bottomNavigationView: BottomNavigationView? = null

  private lateinit var stateViewDelegate: StateViewDelegate<MainStateView>

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

    bindStateDelegate()
  }

  private fun bindStateDelegate() {

    stateViewDelegate = StateViewDelegate(
      State(MainStateView.CONTENT, findViewById<View>(R.id.frame_fragment)),
      State(MainStateView.LOADER, findViewById<View>(R.id.progress_loader))
    )

    val informView = findViewById<View>(R.id.include_inform)
    informView?.let {

      stateViewDelegate.addState(
        State(
          MainStateView.AUTH,
          informView,
          EnterActionStrategy {
            val informImage = it.findViewById<ImageView>(R.id.image_inform)
            val informTitle = it.findViewById<TextView>(R.id.text_inform_title)
            val informBody = it.findViewById<TextView>(R.id.text_inform_body)
            val informButton = it.findViewById<Button>(R.id.btn_inform)

            informTitle.text = string(R.string.msg_yandex_start)
            informBody.text = string(R.string.msg_yandex_account)
            informButton.text = string(R.string.action_yandex_link_account)

            informImage.setImageResource(R.drawable.ic_cloud_off)

            informButton.visibility = View.VISIBLE
            informButton.setOnClickListener {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OAUTH_URL))
              startActivity(intent)
            }
          }
        ),
        State(
          MainStateView.EMPTY,
          informView,
          EnterActionStrategy {
            val informImage = it.findViewById<ImageView>(R.id.image_inform)
            val informTitle = it.findViewById<TextView>(R.id.text_inform_title)
            val informBody = it.findViewById<TextView>(R.id.text_inform_body)
            val informButton = it.findViewById<Button>(R.id.btn_inform)

            informTitle.text = string(R.string.msg_yandex_failed_retrieve)
            informBody.text = string(R.string.action_yandex_check_connection)
            informButton.text = string(R.string.action_yandex_link_account)

            informImage.setImageResource(R.drawable.ic_yandex_disk)
            informButton.visibility = View.GONE
          }
        )
      )
    }

    stateViewDelegate.currentState = MainStateView.CONTENT
  }

  override fun onResume() {
    super.onResume()

    stateViewDelegate.currentState = MainStateView.LOADER
    disposables.addAll(
      presenter
        .getTokenListener()
        .observeOn(foreground())
        .subscribe({
          stateViewDelegate.currentState = (if (it.token.isNotEmpty()) MainStateView.CONTENT else MainStateView.AUTH)
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
        if (token.isNotEmpty()) {
          presenter.saveToken(token)
        }
      }
    }
  }

  /** Create navigation instance.  */
  private fun addBottomNavigation() {
    bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
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
        else -> return@setOnNavigationItemSelectedListener false
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
