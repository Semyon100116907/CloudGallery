package com.semisonfire.cloudgallery.ui.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseActivity;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;
import com.semisonfire.cloudgallery.ui.main.disk.DiskFragment;
import com.semisonfire.cloudgallery.utils.BottomBarUtils;
import com.semisonfire.cloudgallery.utils.FragmentUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity implements MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    //STATE
    private static final String STATE_TITLE = "STATE_TITLE";
    private static final String STATE_CURRENT_FRAGMENT = "STATE_CURRENT_FRAGMENT";

    //Toolbar
    private Toolbar mToolbar;
    private String mTitle;

    //Navigation
    private BottomNavigationView mBottomNavigationView;
    private BaseFragment mFragment;

    //Presenter
    private MainPresenter<MainContract.View> mMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind();
        if (getIntent() != null && getIntent().getData() != null) {
            login();
        }

        if (savedInstanceState != null) {
            mFragment = (BaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_CURRENT_FRAGMENT);
            mTitle = savedInstanceState.getString(STATE_TITLE);
            mToolbar.setTitle(mTitle);
        } else {
            mFragment = new DiskFragment();
        }
        setSupportActionBar(mToolbar);
        FragmentUtils.changeFragment(getSupportFragmentManager(), mFragment, R.id.frame_fragment);
    }

    @Override
    public void bind() {

        //Presenter
        mMainPresenter = new MainPresenter<>(new DiskPreferences(this));
        mMainPresenter.attachView(this);

        //Toolbar
        mToolbar = findViewById(R.id.toolbar);

        //Navigation
        mBottomNavigationView = findViewById(R.id.nav_bottom);
        addBottomNavigation();
    }

    /** Oauth login. */
    private void login() {
        Uri data = getIntent().getData();
        setIntent(null);
        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());
        if (matcher.find()) {
            final String token = matcher.group(1);
            if (!TextUtils.isEmpty(token)) {
                Log.d(TAG, "login: token: " + token);
                saveToken(token);
            } else {
                Log.w(TAG, "success: empty token");
            }
        } else {
            Log.w(TAG, "login: token not found");
        }
    }

    /** Save new token in private. */
    private void saveToken(String token) {
        if (token != null) {
            if (DiskClient.getToken() != null && DiskClient.getToken().equals(token)) {
                return;
            }

            //Recreate api with new token
            DiskClient.getInstance().createApi(this, token);

            mMainPresenter.setCachedToken(token);
        }
    }

    /** Create navigation instance. */
    private void addBottomNavigation() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_disk:
                    mToolbar.setTitle(R.string.msg_disk);
                    mFragment = new DiskFragment();
                    break;
                case R.id.nav_offline:
                    mToolbar.setTitle(R.string.msg_offline);
                    break;
                case R.id.nav_trash:
                    mToolbar.setTitle(R.string.msg_trash);
                    break;
                case R.id.nav_settings:
                    mToolbar.setTitle(R.string.msg_settings);
                    break;
            }
            if (mFragment != null) {
                mTitle = mToolbar.getTitle().toString();
                FragmentUtils.changeFragment(getSupportFragmentManager(), mFragment, R.id.frame_fragment);
            }
            return true;
        });
        mBottomNavigationView.setOnNavigationItemReselectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_disk:
                case R.id.nav_offline:
                case R.id.nav_trash:
                case R.id.nav_settings:
                    mFragment.scrollToTop();
                    break;
            }
        });
        BottomBarUtils.removeShiftingMode(mBottomNavigationView, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, STATE_CURRENT_FRAGMENT, mFragment);
        outState.putString(STATE_TITLE, mTitle);
    }

    @Override
    public void onBackPressed() {
        if (mBottomNavigationView.getSelectedItemId() == R.id.nav_disk) {
            super.onBackPressed();
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.nav_disk);
        }
    }
}
