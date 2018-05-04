package com.semisonfire.cloudgallery.ui.main.trash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.PhotoAdapter;
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TrashFragment extends BaseFragment implements TrashContract.View {

    private static final String TAG = TrashFragment.class.getSimpleName();

    //State
    private static final String STATE_TRASH_LIST = "STATE_TRASH_LIST";
    private static final String STATE_LOADING = "STATE_LOADING";
    private static final String STATE_LAST_PAGE = "STATE_LAST_PAGE";
    private static final String STATE_SELECTABLE = "STATE_SELECTABLE";

    //Presenter
    private TrashPresenter<TrashContract.View> mTrashPresenter;

    //RecyclerView
    private List<Photo> mTrashList;
    private RecyclerView mTrashRecyclerView;
    private PhotoAdapter mTrashPhotoAdapter;

    //Paging
    private int mCurrentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isSelectable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Create remote data manager
        RemoteDataSource remoteDataSource = new RemoteDataSource(DiskClient.getApi());

        //Create local data manager
        //LocalDataSource localDataSource = new LocalDataSource(LocalDatabase.getInstance(context));

        //Create presenter
        mTrashPresenter = new TrashPresenter<>(new DiskPreferences(context), remoteDataSource);
        mTrashPresenter.attachView(this);

        mTrashList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setFrom(PhotoDetailActivity.FROM_TRASH);
        if (savedInstanceState != null) {
            mTrashList = savedInstanceState.getParcelableArrayList(STATE_TRASH_LIST);
            isLoading = savedInstanceState.getBoolean(STATE_LOADING);
            isLastPage = savedInstanceState.getBoolean(STATE_LAST_PAGE);
            isSelectable = savedInstanceState.getBoolean(STATE_SELECTABLE);
            setSelectableItems();
        }
        return inflater.inflate(R.layout.fragment_trash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTrashRecyclerView = view.findViewById(R.id.rv_trash);
        setScrollView(mTrashRecyclerView);

        bind();

        if (mTrashList == null || mTrashList.isEmpty()) {
            isLoading = true;
            mTrashPresenter.getPhotos(mCurrentPage);
        }
    }

    private void setSelectableItems() {
        if (mTrashList != null) {
            for (Photo photo : mTrashList) {
                if (photo.isSelected()) {
                    getSelectedPhotos().add(photo);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_TRASH_LIST, (ArrayList<? extends Parcelable>) mTrashList);
        outState.putBoolean(STATE_LOADING, isLoading);
        outState.putBoolean(STATE_LAST_PAGE, isLastPage);
        outState.putBoolean(STATE_SELECTABLE, isSelectable);
    }

    @Override
    public void bind() {

        getFloatButton().setVisibility(View.GONE);

        //Adapter
        mTrashPhotoAdapter = new PhotoAdapter(this);
        mTrashPhotoAdapter.setPhotos(mTrashList);

        //RecyclerView
        mTrashRecyclerView.setAdapter(mTrashPhotoAdapter);

        //Layout manager
        int orientation = getResources().getConfiguration().orientation;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 3);
        mTrashRecyclerView.setLayoutManager(gridLayoutManager);

        //ItemDecoration
        ItemDecorator mItemDecorator = new ItemDecorator(getResources().getDimensionPixelOffset(R.dimen.disk_grid_space));
        mTrashRecyclerView.addItemDecoration(mItemDecorator);

        //Paging
        mTrashRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    getSwipeRefreshLayout().setEnabled(true);
                } else {
                    getSwipeRefreshLayout().setEnabled(false);
                }

                int position = layoutManager.findLastVisibleItemPosition();
                int limit = DiskClient.MAX_LIMIT;
                int updatePosition = recyclerView.getAdapter().getItemCount() - 1 - limit / 2;

                if (!isLoading && !isLastPage && position >= updatePosition) {
                    isLoading = true;
                    mCurrentPage++;
                    mTrashPresenter.getPhotos(mCurrentPage);
                }
            }
        });

        getSwipeRefreshLayout().setOnRefreshListener(() -> {
            getSwipeRefreshLayout().setRefreshing(true);
            updateDataSet();
        });
    }

    private void updateDataSet() {
        mCurrentPage = 1;
        isLastPage = false;
        isLoading = true;
        mTrashList.clear();
        mTrashPhotoAdapter.setPhotos(mTrashList);
        mTrashPresenter.getPhotos(mCurrentPage);
    }

    private void showEmpty() {
        getStateView().showEmptyView(R.drawable.ic_delete, getString(R.string.msg_yandex_trash_empty), null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.menu_restore_all).setVisible(true);
        menu.findItem(R.id.menu_delete_all).setVisible(true);
        setEnabledSelection(isSelectable);
        mTrashPhotoAdapter.setSelection(isSelectable);
        updateTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setEnabledSelection(false);
                mTrashPhotoAdapter.setSelection(false);
                break;
            case R.id.menu_restore_all:
                if (isSelectable && getSelectedPhotos().size() != mTrashList.size()) {
                    mTrashPresenter.restorePhotos(getSelectedPhotos());
                } else {
                    mTrashPresenter.restorePhotos(mTrashList);
                }
                break;
            case R.id.menu_delete_all:
                if (isSelectable && getSelectedPhotos().size() != mTrashList.size()) {
                    mTrashPresenter.deletePhotos(getSelectedPhotos());
                } else {
                    mTrashPresenter.clear();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEnabledSelection(boolean enabled) {
        isSelectable = enabled;
        SelectableHelper.setMultipleSelection(enabled);
        getSwipeRefreshLayout().setEnabled(!enabled);
        getActionBar().setDisplayHomeAsUpEnabled(enabled);
        getActionBar().setBackgroundDrawable(new ColorDrawable(enabled ? getResources().getColor(R.color.colorAccent)
                : getResources().getColor(R.color.white)));
        if (!enabled) {
            getSelectedPhotos().clear();
            getActionBar().setTitle(R.string.msg_trash);
        }
    }

    @Override
    public void onInternetUnavailable() {
        if (mTrashList.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onTrashLoaded(List<Photo> photos) {
        getSwipeRefreshLayout().setRefreshing(false);
        if (photos != null && !photos.isEmpty()) {
            mTrashList.addAll(photos);
            mTrashPhotoAdapter.addPhotos(photos);
            getStateView().hideStateView();
        } else {
            if (mTrashList.isEmpty()) {
                showEmpty();
            }
            isLastPage = true;
        }
        isLoading = false;
    }

    @Override
    public void onPhotoRestored(Photo photo) {
        mTrashList.remove(photo);
        mTrashPhotoAdapter.remove(photo);

        setEnabledSelection(false);
        mTrashPhotoAdapter.setSelection(false);
        if (mTrashList.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onPhotoDeleted(Photo photo) {
        mTrashList.remove(photo);
        mTrashPhotoAdapter.remove(photo);

        setEnabledSelection(false);
        mTrashPhotoAdapter.setSelection(false);
        if (mTrashList.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onTrashCleared() {
        mTrashList.clear();
        mTrashPhotoAdapter.setPhotos(new ArrayList<>());
        setEnabledSelection(false);
        mTrashPhotoAdapter.setSelection(false);
        showEmpty();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoDetailActivity.DETAIL_REQUEST) {
            boolean isDataChanged = data.getBooleanExtra("isChanged", false);
            if (isDataChanged) {
                updateDataSet();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTrashRecyclerView.setAdapter(null);
        mTrashRecyclerView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTrashPresenter != null) {
            mTrashPresenter.dispose();
        }
    }
}
