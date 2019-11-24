package com.semisonfire.cloudgallery.ui.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.core.mvp.MvpView;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.data.remote.exceptions.InternetUnavailableException;
import com.semisonfire.cloudgallery.data.remote.exceptions.UnauthorizedException;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.custom.StateView;
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.semisonfire.cloudgallery.utils.ColorUtilsKt.setMenuIconsColor;

public abstract class BaseFragment extends Fragment implements MvpView,
        SelectableHelper.OnPhotoListener {

    private ActionBar actionBar;
    private Toolbar toolbar;
    private ViewGroup scrollView;
    private FloatingActionButton floatingActionButton;
    private StateView stateView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String token;
    private List<Photo> selectedPhotos;
    private int from;
    private Menu menu;

    public abstract void bind();

    public abstract void onInternetUnavailable();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        selectedPhotos = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        if (getActivity() != null) {
            stateView = new StateView(getActivity().findViewById(R.id.include_inform));
            stateView.hideStateView();
            floatingActionButton = getActivity().findViewById(R.id.btn_add_new);
            swipeRefreshLayout = getActivity().findViewById(R.id.swipe_refresh);
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            toolbar = getActivity().findViewById(R.id.toolbar);
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            }
        }
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment, menu);
        this.menu = menu;
    }

    public void scrollToTop() {
        if (scrollView != null) {
            if (scrollView instanceof RecyclerView) {
                RecyclerView mRecyclerView = (RecyclerView) scrollView;
                mRecyclerView.smoothScrollToPosition(0);
                return;
            }
            scrollView.scrollTo(0, 0);
        }
    }

    @Override
    public void onPhotoClick(List<Photo> photoList, int position) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), PhotoDetailActivity.class);
            intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position);
            intent.putParcelableArrayListExtra(
                    PhotoDetailActivity.EXTRA_PHOTOS,
                    (ArrayList<? extends Parcelable>) photoList
            );
            intent.putExtra(PhotoDetailActivity.EXTRA_FROM, from);
            startActivityForResult(intent, PhotoDetailActivity.DETAIL_REQUEST);
        }
    }

    public void setEnabledSelection(boolean enabled) {
        SelectableHelper.setMultipleSelection(enabled);

        int secondaryColor = enabled ? getResources().getColor(R.color.white) : getResources()
                .getColor(R.color.black);
        actionBar.setDisplayHomeAsUpEnabled(enabled);
        actionBar.setBackgroundDrawable(new ColorDrawable(enabled ? getResources()
                .getColor(R.color.colorAccent)
                : getResources().getColor(R.color.white)));
        toolbar.setTitleTextColor(secondaryColor);
        setMenuIconsColor(menu, secondaryColor);

        swipeRefreshLayout.setEnabled(!enabled);
    }

    @Override
    public void onPhotoLongClick() {
        setEnabledSelection(true);
    }

    @Override
    public void onSelectedPhotoClick(Photo photo) {
        if (photo.isSelected()) {
            selectedPhotos.add(photo);
        } else {
            selectedPhotos.remove(photo);
        }
        updateTitle();
        if (selectedPhotos.isEmpty()) {
            setEnabledSelection(false);
        }
    }

    public void updateTitle() {
        if (!selectedPhotos.isEmpty()) {
            getActionBar().setTitle(String.valueOf(selectedPhotos.size()));
        }
    }

//    @Override
//    public void onTokenLoaded(String token) {
//        this.token = token;
//        if (TextUtils.isEmpty(token)) {
//            refreshToken();
//        }
//    }

    /**
     * Show state view with snap button.
     */
    private void refreshToken() {
        if (stateView != null) {
            if (floatingActionButton != null) {
                floatingActionButton.hide();
            }
            getStateView().showStateView(R.drawable.ic_cloud_off,
                    getString(R.string.msg_yandex_start),
                    getString(R.string.msg_yandex_account),
                    getString(R.string.action_yandex_link_account), v -> {
                        Intent intent =
                                new Intent(Intent.ACTION_VIEW, Uri.parse(DiskClient.OAUTH_URL));
                        startActivity(intent);
                    }
            );
        }
    }

    @Override
    public void onError(@NotNull Throwable throwable) {
        if (throwable instanceof UnauthorizedException) {
            refreshToken();
            return;
        }

        if (throwable instanceof InternetUnavailableException) {
            if (!TextUtils.isEmpty(token)) {
                onInternetUnavailable();
            }
            Toast.makeText(getContext(), R.string.msg_internet_disable, Toast.LENGTH_LONG).show();
        }
    }

    public void setFrom(int mFrom) {
        this.from = mFrom;
    }

    public void setScrollView(ViewGroup scrollView) {
        this.scrollView = scrollView;
    }

    public List<Photo> getSelectedPhotos() {
        return selectedPhotos;
    }

    public StateView getStateView() {
        return stateView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public FloatingActionButton getFloatButton() {
        return floatingActionButton;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }
}
