package com.semisonfire.cloudgallery.ui.main.trash;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator;
import com.semisonfire.cloudgallery.ui.custom.PaginationScrollListener;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.main.dialogs.AlertDialogFragment;
import com.semisonfire.cloudgallery.ui.main.dialogs.base.DialogListener;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.PhotoAdapter;
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TrashFragment extends BaseFragment implements TrashContract.View, DialogListener {

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

        int orientation = getResources().getConfiguration().orientation;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 3);
        mTrashRecyclerView.setLayoutManager(gridLayoutManager);

        ItemDecorator mItemDecorator = new ItemDecorator(getResources().getDimensionPixelOffset(R.dimen.disk_grid_space));
        mTrashRecyclerView.addItemDecoration(mItemDecorator);

        //Paging recycler view
        mTrashRecyclerView.addOnScrollListener(new PaginationScrollListener(DiskClient.MAX_LIMIT) {
            @Override
            public void loadNext() {
                isLoading = true;
                mCurrentPage++;
                mTrashPresenter.getPhotos(mCurrentPage);
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        });
        mTrashRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition =
                        recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                getSwipeRefreshLayout().setEnabled(topRowVerticalPosition >= 0 && !isSelectable);
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
                    if (!mTrashList.isEmpty() || !getSelectedPhotos().isEmpty()) {
                        showDialog(getString(R.string.msg_clear_trash),
                                getString(R.string.msg_clear_trash_description),
                                getResources().getColor(R.color.colorAccent));
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEnabledSelection(boolean enabled) {
        super.setEnabledSelection(enabled);
        isSelectable = enabled;
        mTrashPhotoAdapter.setSelection(enabled);
        if (!enabled) {
            getSelectedPhotos().clear();
            getActionBar().setTitle(R.string.msg_trash);
        }
    }

    @Override
    public void onInternetUnavailable() {
        if (mTrashList.isEmpty()) {
            getStateView().showEmptyView(R.drawable.ic_delete,
                    getString(R.string.msg_yandex_failed_retrieve),
                    getString(R.string.action_yandex_check_connection));
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

        Toast.makeText(getContext(), getString(R.string.msg_photo) + " "
                + photo.getName() + " " + getString(R.string.msg_restored).toLowerCase(), Toast.LENGTH_LONG).show();

        setEnabledSelection(false);
        if (mTrashList.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onPhotoDeleted(Photo photo) {
        mTrashList.remove(photo);
        mTrashPhotoAdapter.remove(photo);

        Toast.makeText(getContext(), getString(R.string.msg_photo) + " "
                + photo.getName() + " " + getString(R.string.msg_deleted).toLowerCase(), Toast.LENGTH_LONG).show();

        setEnabledSelection(false);
        if (mTrashList.isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void onTrashCleared() {
        mTrashList.clear();
        mTrashPhotoAdapter.setPhotos(new ArrayList<>());
        setEnabledSelection(false);
        showEmpty();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoDetailActivity.DETAIL_REQUEST) {
            boolean isDataChanged = data.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false);
            if (isDataChanged) {
                updateDataSet();
            }
        }
    }

    private void showDialog(String title, String message, int color) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            AlertDialogFragment mAlertDialog = AlertDialogFragment.newInstance(title, message, color);
            mAlertDialog.setTargetFragment(this, 0);
            mAlertDialog.show(activity.getSupportFragmentManager(), "alert");
        }
    }

    @Override
    public void onPositiveClick(DialogInterface dialogInterface) {
        mTrashPresenter.clear();
        dialogInterface.cancel();
    }

    @Override
    public void onNegativeClick(DialogInterface dialogInterface) {
        dialogInterface.cancel();
    }

    @Override
    public void onItemClick(DialogInterface dialogInterface, View view) {

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
