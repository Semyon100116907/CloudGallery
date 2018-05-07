package com.semisonfire.cloudgallery.ui.main.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.ui.about.AboutActivity;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;

public class SettingsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind();
    }

    @Override
    public void bind() {
        getFloatButton().setVisibility(View.GONE);
        getSwipeRefreshLayout().setEnabled(false);
        TextView action = getView().findViewById(R.id.text_settings_action);
        action.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onTokenLoaded(String token) {
        /* Not needed */
    }

    @Override
    public void onInternetUnavailable() {
        /* Not require because data is not requested */
    }
}
