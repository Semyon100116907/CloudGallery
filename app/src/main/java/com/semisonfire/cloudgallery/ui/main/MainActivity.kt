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
import java.util.regex.Pattern

class MainActivity : BaseActivity<MainContract.View, MainContract.Presenter>(), MainContract.View {

  private var toolbar: Toolbar? = null
  private var title: String = ""

  private var bottomNavigationView: BottomNavigationView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (intent != null && intent.data != null) {
      login()
    }

    title = if (savedInstanceState != null) {

      savedInstanceState.getString(STATE_TITLE) ?: ""
    } else {
      getString(R.string.msg_disk)
    }
    toolbar?.title = title
    setSupportActionBar(toolbar)

    router.replaceScreen(DISK_KEY)
  }

  public override fun bind() {
    super.bind()

    toolbar = findViewById(R.id.toolbar)

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
    bottomNavigationView!!.setOnNavigationItemSelectedListener { item ->
      val key = when (item.itemId) {
        R.id.nav_disk -> {
          toolbar?.setTitle(R.string.msg_disk)
          DISK_KEY
        }
        R.id.nav_trash -> {
          toolbar?.setTitle(R.string.msg_trash)
          TRASH_KEY
        }
        R.id.nav_settings -> {
          toolbar?.setTitle(R.string.msg_settings)
          SETTINGS_KEY
        }
        else -> {
          null
        }
      }
      key?.let {
        title = toolbar?.title.toString()
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
    outState.putString(STATE_TITLE, title)
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
    private const val STATE_TITLE = "STATE_TITLE"
  }
}
