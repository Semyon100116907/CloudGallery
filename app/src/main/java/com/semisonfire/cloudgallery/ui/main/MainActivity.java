package com.semisonfire.cloudgallery.ui.main;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.core.ui.BaseActivity;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;
import com.semisonfire.cloudgallery.ui.main.disk.DiskFragment;
import com.semisonfire.cloudgallery.ui.main.settings.SettingsFragment;
import com.semisonfire.cloudgallery.ui.main.trash.TrashFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity<MainContract.View, MainContract.Presenter> implements MainContract.View {

    //STATE
    private static final String STATE_TITLE = "STATE_TITLE";
    private static final String STATE_CURRENT_FRAGMENT = "STATE_CURRENT_FRAGMENT";

    private Toolbar toolbar;
    private String title;

    private BottomNavigationView bottomNavigationView;
    private BaseFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getData() != null) {
            login();
        }

        if (savedInstanceState != null) {
            fragment = (BaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_CURRENT_FRAGMENT);
            title = savedInstanceState.getString(STATE_TITLE);
        } else {
            fragment = new DiskFragment();
            title = getString(R.string.msg_disk);
        }
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
//        FragmentUtils.changeFragment(getSupportFragmentManager(), fragment, R.id.frame_fragment);
    }

    @Override
    public void bind() {
        super.bind();

        toolbar = findViewById(R.id.toolbar);

        bottomNavigationView = findViewById(R.id.nav_bottom);
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
//                saveToken(token);
            }
        }
    }

    /** Save new token in private. */
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

    /** Create navigation instance. */
    private void addBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_disk:
                    toolbar.setTitle(R.string.msg_disk);
                    fragment = new DiskFragment();
                    break;
                case R.id.nav_trash:
                    toolbar.setTitle(R.string.msg_trash);
                    fragment = new TrashFragment();
                    break;
                case R.id.nav_settings:
                    toolbar.setTitle(R.string.msg_settings);
                    fragment = new SettingsFragment();
                    break;
            }
            if (fragment != null) {
                title = toolbar.getTitle().toString();
//                FragmentUtils.changeFragment(getSupportFragmentManager(), fragment, R.id.frame_fragment);
            }
            return true;
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_disk:
                case R.id.nav_trash:
                    fragment.scrollToTop();
                    break;
                case R.id.nav_settings:
                    break;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, STATE_CURRENT_FRAGMENT, fragment);
        outState.putString(STATE_TITLE, title);
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_disk) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_disk);
        }
    }

    @Override
    public int layout() {
        return R.layout.activity_main;
    }
}
