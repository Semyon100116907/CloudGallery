package com.semisonfire.cloudgallery.ui.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.BaseActivity
import com.semisonfire.cloudgallery.ui.main.disk.DISK_KEY
import com.semisonfire.cloudgallery.ui.main.settings.SETTINGS_KEY
import com.semisonfire.cloudgallery.ui.main.trash.TRASH_KEY
import com.semisonfire.cloudgallery.utils.string
import java.util.regex.Pattern

class MainActivity : BaseActivity<MainContract.View, MainContract.Presenter>(), MainContract.View {

  private var toolbar: Toolbar? = null
  private var bottomNavigationView: BottomNavigationView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (intent != null && intent.data != null) {
      login()
    }

    val currentScreenKey: String = if (savedInstanceState != null) {
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
  }

  /** Oauth login.  */
  private fun login() {
    val data = intent.data
    intent = null
    val pattern = Pattern.compile("access_token=(.*?)(&|$)")
    val matcher = pattern.matcher(data!!.toString())
    if (matcher.find()) {
      val token = matcher.group(1)
      if (!TextUtils.isEmpty(token)) {
        //                saveToken(token);
      }
    }
  }

  /** Save new token in private.  */
  //    private void saveToken(String token) {
  //        if (token != null) {
  //            if (DiskClient.getToken() != null && DiskClient.getToken().equals(token)) {
  //                return;
  //            }
  //
  //            //Update token
  //            DiskClient.getInstance().getAuthInterceptor().setToken(token);
  //
  //            //Save new token
  //            mMainPresenter.setCachedToken(token);
  //        }
  //    }

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
    bottomNavigationView?.setOnNavigationItemReselectedListener { item ->
      //      when (item.itemId) {
//        R.id.nav_disk, R.id.nav_trash -> fragment?.scrollToTop()
//        R.id.nav_settings -> {
//        }
//      }
    }
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
