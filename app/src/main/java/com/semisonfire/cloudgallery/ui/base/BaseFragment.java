package com.semisonfire.cloudgallery.ui.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException;
import com.semisonfire.cloudgallery.data.remote.exceptions.UnauthorizedException;
import com.semisonfire.cloudgallery.ui.custom.StateView;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragment extends Fragment implements MvpView, SelectableHelper.OnPhotoListener {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private ActionBar mActionBar;
    private ViewGroup mScrollView;
    private FloatingActionButton mFloatButton;
    private StateView mStateView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BasePresenter<MvpView> mBasePresenter;

    private List<Photo> mSelectedPhotos;
    private int mFrom;

    public abstract void bind();

    public abstract void onInternetUnavailable();

    public abstract void setEnabledSelection(boolean enabled);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBasePresenter = new BasePresenter<>(new DiskPreferences(context));
        mBasePresenter.attachView(this);
        mSelectedPhotos = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            mStateView = new StateView(getActivity().findViewById(R.id.include_inform));
            mStateView.hideStateView();
            mFloatButton = getActivity().findViewById(R.id.btn_add_new);
            mSwipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh);
            mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (mActionBar != null) {
                mActionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            }
        }
        setHasOptionsMenu(true);
        mBasePresenter.getCachedToken();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    public void scrollToTop() {
        if (mScrollView != null) {
            if (mScrollView instanceof RecyclerView) {
                RecyclerView mRecyclerView = (RecyclerView) mScrollView;
                mRecyclerView.smoothScrollToPosition(0);
                return;
            }
            mScrollView.scrollTo(0, 0);
        }
    }

    @Override
    public void onPhotoClick(List<Photo> photoList, int position) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), PhotoDetailActivity.class);
            intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position);
            intent.putParcelableArrayListExtra(PhotoDetailActivity.EXTRA_PHOTOS, (ArrayList<? extends Parcelable>) photoList);
            intent.putExtra(PhotoDetailActivity.EXTRA_FROM, mFrom);
            startActivityForResult(intent, PhotoDetailActivity.DETAIL_REQUEST);
        }
    }

    @Override
    public void onPhotoLongClick() {
        setEnabledSelection(true);
    }

    @Override
    public void onSelectedPhotoClick(Photo photo) {
        if (photo.isSelected()) {
            mSelectedPhotos.add(photo);
        } else {
            mSelectedPhotos.remove(photo);
        }
        updateTitle();
        if (mSelectedPhotos.isEmpty()) {
            setEnabledSelection(false);
        }
    }

    public void updateTitle() {
        if (!mSelectedPhotos.isEmpty()) {
            getActionBar().setTitle(String.valueOf(mSelectedPhotos.size()));
        }
    }

    @Override
    public void onTokenLoaded(String token) {
        if (token == null) {
            refreshToken();
        }
    }

    /** Show state view with snap button. */
    private void refreshToken() {
        if (mStateView != null) {
            if (mFloatButton != null) {
                mFloatButton.setVisibility(View.GONE);
            }
            getStateView().showStateView(R.drawable.ic_cloud_off,
                    getString(R.string.msg_yandex_start),
                    getString(R.string.msg_yandex_account),
                    getString(R.string.action_yandex_link_account), v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DiskClient.OAUTH_URL));
                        startActivity(intent);
                    });
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null) {
            if (throwable instanceof UnauthorizedException) {
                mBasePresenter.setCachedToken("");
                refreshToken();
                return;
            }

            if (throwable instanceof InternetUnavailableException) {
                onInternetUnavailable();
                return;
            }
            Log.e(TAG, "onError: " + throwable.getMessage(), throwable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBasePresenter != null) {
            mBasePresenter.dispose();
        }
    }

    public void setFrom(int mFrom) {
        this.mFrom = mFrom;
    }

    public void setScrollView(ViewGroup scrollView) {
        mScrollView = scrollView;
    }

    public List<Photo> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    public StateView getStateView() {
        return mStateView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public FloatingActionButton getFloatButton() {
        return mFloatButton;
    }

    public ActionBar getActionBar() {
        return mActionBar;
    }
}
