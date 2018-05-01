package com.semisonfire.cloudgallery.ui.custom;

import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.semisonfire.cloudgallery.R;

public class StateView {

    private View mStateView;

    private ViewGroup mContainer;

    private ImageView mInformImage;
    private TextView mTextTitle;
    private TextView mTextBody;
    private Button mInformBtn;

    public StateView(View stateView) {
        this.mStateView = stateView;
        if (mStateView != null) {
            mContainer = mStateView.findViewById(R.id.container_inform);
            mInformImage = mContainer.findViewById(R.id.image_inform);
            mTextTitle = mContainer.findViewById(R.id.text_inform_title);
            mTextBody = mContainer.findViewById(R.id.text_inform_body);
            mInformBtn = mContainer.findViewById(R.id.btn_inform);
        }
    }

    public void showEmptyView(@DrawableRes int imgId, String title, String body) {
        showStateView(imgId, title, body, null, null);
    }

    public void showStateView(@DrawableRes int imgId,
                              @NonNull String title,
                              @Nullable String body,
                              @Nullable String action,
                              @Nullable View.OnClickListener clickListener) {

        if (mStateView != null) {
            mStateView.setVisibility(View.VISIBLE);
            Resources res = mStateView.getResources();
            if (mContainer != null) {
                mInformImage.setImageDrawable(res.getDrawable(imgId));
                mTextTitle.setText(title);
                bindNullableViews(body, action, clickListener);
            }
        }
    }

    private void bindNullableViews(@Nullable String body,
                                   @Nullable String action,
                                   @Nullable View.OnClickListener clickListener) {

        if (body != null && body.length() > 0) {
            mTextBody.setText(body);
            mTextBody.setVisibility(View.VISIBLE);
        } else {
            mTextBody.setVisibility(View.GONE);
        }
        if (action != null && action.length() > 0 && clickListener != null) {
            mInformBtn.setText(action);
            mInformBtn.setOnClickListener(clickListener);
            mInformBtn.setVisibility(View.VISIBLE);
        } else {
            mInformBtn.setVisibility(View.GONE);
        }
    }

    public void hideStateView() {
        if (mStateView != null) {
            mStateView.setVisibility(View.GONE);
        }
    }
}
