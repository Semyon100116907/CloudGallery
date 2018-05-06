package com.semisonfire.cloudgallery.ui.about;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.semisonfire.cloudgallery.BuildConfig;
import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.ui.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        bind();
    }

    @Override
    public void bind() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView version = findViewById(R.id.text_version);
        version.setText(String.format("%s %s", getString(R.string.msg_version), BuildConfig.VERSION_NAME));
    }
}
