package com.semisonfire.cloudgallery.ui.main.disk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.local.LocalDataSource;
import com.semisonfire.cloudgallery.data.local.LocalDatabase;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseFragment;
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator;
import com.semisonfire.cloudgallery.ui.custom.PaginationScrollListener;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.main.dialogs.AlertDialogFragment;
import com.semisonfire.cloudgallery.ui.main.dialogs.BottomDialogFragment;
import com.semisonfire.cloudgallery.ui.main.dialogs.base.DialogListener;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.DiskAdapter;
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity;
import com.semisonfire.cloudgallery.utils.FileUtils;
import com.semisonfire.cloudgallery.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class DiskFragment extends BaseFragment implements DiskContract.View, DialogListener {

    private static final String TAG = DiskFragment.class.getSimpleName();

    //Saved state constants
    private static final String STATE_FILE_URI = "STATE_FILE_URI";
    private static final String STATE_INTENT = "STATE_INTENT";
    private static final String STATE_PHOTOS = "STATE_PHOTOS";
    private static final String STATE_REQUEST = "STATE_REQUEST";
    private static final String STATE_UPLOADING_PHOTOS = "STATE_UPLOADING_PHOTOS";
    private static final String STATE_LOADING = "STATE_LOADING";
    private static final String STATE_LAST_PAGE = "STATE_LAST_PAGE";
    private static final String STATE_SELECTABLE = "STATE_SELECTABLE";

    //Requests types
    private static final int GALLERY_IMAGE_REQUEST = 1999;
    private static final int MEMORY_REQUEST = 2000;
    private static final int CAMERA_REQUEST = 1888;
    private int CURRENT_REQUEST;

    //Dialog types
    private static final String BOTTOM = "BOTTOM";
    private static final String ALERT = "ALERT";

    //Presenter
    private DiskPresenter<DiskContract.View> mDiskPresenter;

    //RecyclerView
    private RecyclerView mDiskRecyclerView;
    private DiskAdapter mDiskAdapter;
    private List<Photo> mPhotoList;
    private Menu menu;

    //Uploading
    private List<Photo> mUploadingList;

    //Paging
    private int mCurrentPage = 1;
    private boolean isLastPage = false;
    private boolean isLoading = true;

    //Camera/gallery request
    private Uri mCameraFileUri;
    private Intent mResultIntent;
    private boolean isSelectable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Shared preference
        DiskPreferences preferences = new DiskPreferences(context);

        //Get disk api
        RemoteDataSource remoteDataSource = new RemoteDataSource(DiskClient.getApi());

        //Create local data manager
        LocalDataSource localDataSource = new LocalDataSource(LocalDatabase.getInstance(context));

        //Create presenter
        mDiskPresenter = new DiskPresenter<>(preferences, remoteDataSource, localDataSource);
        mDiskPresenter.attachView(this);

        mPhotoList = new ArrayList<>();
        mUploadingList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setFrom(PhotoDetailActivity.FROM_DISK);
        //region SavedInstance
        if (savedInstanceState != null) {
            mResultIntent = savedInstanceState.getParcelable(STATE_INTENT);
            mPhotoList = savedInstanceState.getParcelableArrayList(STATE_PHOTOS);
            mUploadingList = savedInstanceState.getParcelableArrayList(STATE_UPLOADING_PHOTOS);
            mCameraFileUri = savedInstanceState.getParcelable(STATE_FILE_URI);
            CURRENT_REQUEST = savedInstanceState.getInt(STATE_REQUEST);
            isLoading = savedInstanceState.getBoolean(STATE_LOADING);
            isLastPage = savedInstanceState.getBoolean(STATE_LAST_PAGE);
            isSelectable = savedInstanceState.getBoolean(STATE_SELECTABLE);
            if (mResultIntent != null) {
                onActivityResult(CURRENT_REQUEST, Activity.RESULT_OK, mResultIntent);
            }

            setSelectableItems();
        }
        //endregion
        return inflater.inflate(R.layout.fragment_disk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDiskRecyclerView = view.findViewById(R.id.rv_disk);
        setScrollView(mDiskRecyclerView);

        bind();

        //Get uploading photos
        if (mUploadingList != null && !mUploadingList.isEmpty()) {
            mDiskAdapter.addUploadPhotos(mUploadingList);
            mDiskPresenter.uploadPhotos(mUploadingList);
        }

        //Get remote photos
        if (mPhotoList == null || mPhotoList.isEmpty()) {
            isLoading = true;
            mDiskPresenter.getPhotos(mCurrentPage);
        }
    }

    private void setSelectableItems() {
        if (mPhotoList != null) {
            for (Photo photo : mPhotoList) {
                if (photo.isSelected()) {
                    getSelectedPhotos().add(photo);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_INTENT, mResultIntent);
        outState.putParcelableArrayList(STATE_PHOTOS, (ArrayList<? extends Parcelable>) mPhotoList);
        outState.putParcelableArrayList(STATE_UPLOADING_PHOTOS, (ArrayList<? extends Parcelable>) mUploadingList);
        outState.putParcelable(STATE_FILE_URI, mCameraFileUri);
        outState.putInt(STATE_REQUEST, CURRENT_REQUEST);
        outState.putBoolean(STATE_LOADING, isLoading);
        outState.putBoolean(STATE_LAST_PAGE, isLastPage);
        outState.putBoolean(STATE_SELECTABLE, isSelectable);
    }

    @Override
    public void bind() {

        getFloatButton().setVisibility(View.VISIBLE);
        getFloatButton().setOnClickListener(v -> setBottomDialog());

        mDiskAdapter = new DiskAdapter(this);
        mDiskAdapter.setPhotos(mPhotoList);
        mDiskRecyclerView.setAdapter(mDiskAdapter);
        mDiskRecyclerView.getItemAnimator().setChangeDuration(0);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mDiskRecyclerView.setLayoutManager(mLinearLayoutManager);

        ItemDecorator mItemDecorator = new ItemDecorator(getResources().getDimensionPixelOffset(R.dimen.disk_linear_space));
        mDiskRecyclerView.addItemDecoration(mItemDecorator);

        mDiskRecyclerView.addOnScrollListener(new PaginationScrollListener(DiskClient.MAX_LIMIT) {
            @Override
            public void loadNext() {
                isLoading = true;
                mCurrentPage++;
                mDiskPresenter.getPhotos(mCurrentPage);
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

        //Paging recycler view
        mDiskRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition =
                        recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                getSwipeRefreshLayout().setEnabled(topRowVerticalPosition >= 0);
            }
        });

        getSwipeRefreshLayout().setOnRefreshListener(() -> {
            getSwipeRefreshLayout().setRefreshing(true);
            updateDataSet();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        setEnabledSelection(isSelectable);
        mDiskAdapter.setSelection(isSelectable);
        updateTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelSelection();
                break;
            case R.id.menu_download:
                if (checkPermission(MEMORY_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    mDiskPresenter.downloadPhotos(getSelectedPhotos());
                    cancelSelection();
                }
                break;
            case R.id.menu_delete:
                mDiskPresenter.deletePhotos(getSelectedPhotos());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEnabledSelection(boolean enabled) {
        isSelectable = enabled;
        SelectableHelper.setMultipleSelection(enabled);
        menu.findItem(R.id.menu_delete).setVisible(enabled);
        menu.findItem(R.id.menu_download).setVisible(enabled);
        getFloatButton().setVisibility(enabled ? View.GONE : View.VISIBLE);
        getActionBar().setDisplayHomeAsUpEnabled(enabled);
        getActionBar().setBackgroundDrawable(new ColorDrawable(enabled ? getResources().getColor(R.color.colorAccent)
                : getResources().getColor(R.color.white)));
        getSwipeRefreshLayout().setEnabled(!enabled);

        if (!enabled) {
            getSelectedPhotos().clear();
            getActionBar().setTitle(R.string.msg_disk);
            mDiskAdapter.setSelection(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            List<Photo> mPhotos = new ArrayList<>();
            switch (requestCode) {
                case GALLERY_IMAGE_REQUEST:
                    extractFromGallery(data, mPhotos);
                    mDiskAdapter.addUploadPhotos(mPhotos);
                    mDiskPresenter.uploadPhotos(mPhotos);
                    break;
                case CAMERA_REQUEST:
                    if (mCameraFileUri != null) {
                        Photo photo = getLocalPhoto(mCameraFileUri);
                        if (photo != null) {
                            mPhotos.add(photo);
                            mDiskAdapter.addUploadPhotos(mPhotos);
                            mDiskPresenter.uploadPhoto(photo);
                        }
                    }
                    break;
                case PhotoDetailActivity.DETAIL_REQUEST:
                    boolean isDataChanged = data.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false);
                    if (isDataChanged) {
                        updateDataSet();
                    }
                    return;
            }
            mUploadingList.addAll(mPhotos);

            mResultIntent = null;
            getStateView().hideStateView();
            scrollToTop();
        }
    }

    private void updateDataSet() {
        mCurrentPage = 1;
        isLastPage = false;
        isLoading = true;
        mPhotoList.clear();
        mDiskAdapter.clear();
        if (!mUploadingList.isEmpty()) {
            mDiskAdapter.addUploadPhotos(mUploadingList);
        }
        mDiskPresenter.getPhotos(mCurrentPage);
    }

    private void extractFromGallery(Intent data, List<Photo> mPhotos) {
        if (data.getClipData() == null) {
            if (data.getData() == null) {
                return;
            }
            Uri imageUri = data.getData();
            mPhotos.add(getLocalPhoto(imageUri));
            return;
        }
        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
            Uri imageUri = data.getClipData().getItemAt(i).getUri();
            mPhotos.add(getLocalPhoto(imageUri));
        }
    }

    /** Get local file path. */
    private Photo getLocalPhoto(Uri contentUri) {
        if (getActivity() != null) {
            String path = contentUri.toString();

            Cursor cursor = getActivity().getContentResolver().query(contentUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                if (idx != -1) {
                    path = cursor.getString(idx);
                    cursor.close();
                } else {
                    path = FileUtils.getInstance().getFile(contentUri).getPath();
                }
            }

            Photo photo = new Photo();
            photo.setUploaded(false);
            photo.setPreview(contentUri.toString());
            photo.setLocalPath(path);
            photo.setName(path.substring(path.lastIndexOf('/') + 1));
            if (mPhotoList.contains(photo)) {
                int index = photo.getName().lastIndexOf('.');
                String name = photo.getName().substring(0, index);
                String extension = photo.getName().substring(index);
                photo.setName(copyName(name, extension, 0));
            }
            return photo;
        }
        return null;
    }

    /** Check file for duplicates. */
    private String copyName(String name, String extension, int counter) {
        Photo photo = new Photo();

        String newName = name;
        if (counter != 0) {
            newName = name + "(" + String.valueOf(counter) + ")";
        }
        newName += extension;
        photo.setName(newName);

        if (mPhotoList.contains(photo)) {
            counter++;
            return copyName(name, extension, counter);
        }

        return newName;
    }

    @Override
    public void onInternetUnavailable() {
        if (mPhotoList.isEmpty()) {
            getStateView().showEmptyView(R.drawable.ic_yandex_disk,
                    getString(R.string.msg_yandex_failed_retrieve),
                    getString(R.string.action_yandex_check_connection));
        }
    }

    @Override
    public void onPhotosLoaded(List<Photo> photos) {
        getSwipeRefreshLayout().setRefreshing(false);
        if (photos != null && !photos.isEmpty()) {
            getFloatButton().setVisibility(View.VISIBLE);
            mPhotoList.addAll(photos);
            mDiskAdapter.addPhotos(photos);
            getStateView().hideStateView();
        } else {
            if (mPhotoList.isEmpty()) {
                getStateView().showEmptyView(R.drawable.ic_yandex_disk,
                        getString(R.string.msg_yandex_ready),
                        getString(R.string.action_yandex_add_items));
            }
            isLastPage = true;
        }
        isLoading = false;
    }

    @Override
    public void onPhotoUploaded(Photo photo) {
        String uploadState = null;
        if (photo != null) {
            mPhotoList.add(photo);
            mDiskAdapter.addPhoto(photo);
            mUploadingList.remove(photo);
            mDiskAdapter.removeUploadedPhoto(photo);
        } else {
            uploadState = getString(R.string.msg_wait);
        }
        mDiskAdapter.changeUploadState(uploadState);
    }

    @Override
    public void onPhotoDownloaded(String path) {
        Toast.makeText(getContext(), path, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPhotoDeleted(Photo photo) {
        cancelSelection();
        mPhotoList.remove(photo);
        mDiskAdapter.removePhoto(photo);
        Toast.makeText(getContext(), photo.getName(), Toast.LENGTH_LONG).show();
    }

    private void cancelSelection() {
        setEnabledSelection(false);
        mDiskAdapter.setSelection(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        int color = getResources().getColor(R.color.colorAccent);
        String title = getString(R.string.msg_request_permission);
        String body;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    createCameraIntent();
                    break;
                case GALLERY_IMAGE_REQUEST:
                    createGalleryIntent();
                    break;
                case MEMORY_REQUEST:
                    mDiskPresenter.downloadPhotos(getSelectedPhotos());
                    cancelSelection();
                    break;
            }
        } else {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    body = getString(R.string.msg_camera_permission);
                    break;
                case GALLERY_IMAGE_REQUEST:
                case MEMORY_REQUEST:
                    body = getString(R.string.msg_memory_permission);
                    break;
                default:
                    body = null;
                    break;
            }
            if (!PermissionUtils.shouldShowRational(getActivity(), permissions[0])) {
                permissionDialog(title, body, color);
            }
        }
    }

    //region Dialogs
    private void setBottomDialog() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            BottomDialogFragment mBottomDialog = new BottomDialogFragment();
            mBottomDialog.setTargetFragment(this, 0);
            mBottomDialog.show(activity.getSupportFragmentManager(), BOTTOM);
        }
    }

    private void permissionDialog(String title, String message, int color) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            AlertDialogFragment mAlertDialog = AlertDialogFragment.newInstance(title, message, color);
            mAlertDialog.setTargetFragment(this, 0);
            mAlertDialog.show(activity.getSupportFragmentManager(), ALERT);
        }
    }

    private boolean checkPermission(int request, String... permission) {
        if (!PermissionUtils.hasPermission(getActivity(), permission[0])) {
            PermissionUtils.requestPermissions(this, permission, request);
            return false;
        }
        return true;
    }

    @Override
    public void onPositiveClick(DialogInterface dialogInterface) {
        Activity activity = getActivity();
        if (activity != null) {
            PermissionUtils.goToAppSettings(activity);
        }
        dialogInterface.cancel();
    }

    @Override
    public void onNegativeClick(DialogInterface dialogInterface) {
        dialogInterface.cancel();
    }

    @Override
    public void onItemClick(DialogInterface dialogInterface, View view) {
        int id = view.getId();
        dialogInterface.cancel();
        switch (id) {
            case R.id.container_camera:
                if (!checkPermission(CAMERA_REQUEST, Manifest.permission.CAMERA)) {
                    return;
                }
                createCameraIntent();
                CURRENT_REQUEST = CAMERA_REQUEST;
                break;
            case R.id.container_gallery:
                if (!checkPermission(GALLERY_IMAGE_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                createGalleryIntent();
                CURRENT_REQUEST = GALLERY_IMAGE_REQUEST;
                break;
        }
    }

    private void createCameraIntent() {
        mResultIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraFileUri = FileUtils.getInstance().getLocalFileUri();
        mResultIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraFileUri);
        startActivityForResult(mResultIntent, CAMERA_REQUEST);
    }

    private void createGalleryIntent() {
        mResultIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mResultIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(mResultIntent,
                getString(R.string.msg_image_chooser)),
                GALLERY_IMAGE_REQUEST);
    }
    //endregion

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDiskRecyclerView.setAdapter(null);
        mDiskRecyclerView = null;
    }

    @Override
    public void onDestroy() {
        if (mDiskPresenter != null) {
            mDiskPresenter.dispose();
        }
        super.onDestroy();
    }
}