package com.semisonfire.cloudgallery.ui.main.disk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback;
import com.semisonfire.cloudgallery.core.ui.BaseFragment;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.semisonfire.cloudgallery.utils.ColorUtilsKt.setMenuIconsColor;

@SuppressWarnings("unchecked")
public class DiskFragment extends BaseFragment<DiskContract.View, DiskContract.Presenter> implements
        DiskContract.View, DialogListener {

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

    //Dialog types
    private static final String BOTTOM = "BOTTOM";
    private static final String ALERT = "ALERT";

    //RecyclerView
    private RecyclerView recyclerView;
    private DiskAdapter diskAdapter;
    private List<Photo> photoList;
    private Menu menu;

    //Uploading
    private List<Photo> uploadingList;

    //Paging
    private int mCurrentPage = 1;
    private boolean isLastPage = false;
    private boolean isLoading = true;

    //Camera/gallery request
    private Uri cameraFileUri;
    private Intent resultIntent;
    private boolean isSelectable;

    private List<Photo> selectedPhotos = new ArrayList<>();
    private FloatingActionButton floatingActionButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        photoList = new ArrayList<>();
        uploadingList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreateView(inflater, container, savedInstanceState);
        //region SavedInstance
        if (savedInstanceState != null) {
            resultIntent = savedInstanceState.getParcelable(STATE_INTENT);
            photoList = savedInstanceState.getParcelableArrayList(STATE_PHOTOS);
            uploadingList = savedInstanceState.getParcelableArrayList(STATE_UPLOADING_PHOTOS);
            cameraFileUri = savedInstanceState.getParcelable(STATE_FILE_URI);
            isLoading = savedInstanceState.getBoolean(STATE_LOADING);
            isLastPage = savedInstanceState.getBoolean(STATE_LAST_PAGE);
            isSelectable = savedInstanceState.getBoolean(STATE_SELECTABLE);

            setSelectableItems();
        }
        //endregion
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setSelectableItems() {
        if (photoList != null) {
            for (Photo photo : photoList) {
                if (photo.isSelected()) {
                    selectedPhotos.add(photo);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_INTENT, resultIntent);
        outState.putParcelableArrayList(STATE_PHOTOS, (ArrayList<? extends Parcelable>) photoList);
        outState.putParcelableArrayList(
                STATE_UPLOADING_PHOTOS,
                (ArrayList<? extends Parcelable>) uploadingList
        );
        outState.putParcelable(STATE_FILE_URI, cameraFileUri);
        outState.putBoolean(STATE_LOADING, isLoading);
        outState.putBoolean(STATE_LAST_PAGE, isLastPage);
        outState.putBoolean(STATE_SELECTABLE, isSelectable);
    }

    @Override
    public void bind(@NotNull View view) {
        super.bind(view);
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            floatingActionButton = activity.findViewById(R.id.btn_add_new);
            swipeRefreshLayout = activity.findViewById(R.id.swipe_refresh);
        }
        recyclerView = view.findViewById(R.id.rv_disk);
        presenter.getUploadingPhotos();

        //Get remote photos
        if (photoList == null || photoList.isEmpty()) {
            isLoading = true;
            presenter.getPhotos(mCurrentPage);
        }

        floatingActionButton.show();
        floatingActionButton.setOnClickListener(v -> setBottomDialog());

        diskAdapter = new DiskAdapter(new SelectableHelper.OnPhotoListener() {
            @Override
            public void onPhotoClick(List<Photo> photos, int position) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), PhotoDetailActivity.class);
                    intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position);
                    intent.putParcelableArrayListExtra(
                            PhotoDetailActivity.EXTRA_PHOTOS,
                            (ArrayList<? extends Parcelable>) photoList
                    );
                    intent.putExtra(PhotoDetailActivity.EXTRA_FROM, PhotoDetailActivity.FROM_DISK);
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
                    selectedPhotos.add(photo);
                } else {
                    selectedPhotos.remove(photo);
                }
                updateToolbarTitle(String.valueOf(selectedPhotos.size()));
                if (selectedPhotos.isEmpty()) {
                    setEnabledSelection(false);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setAdapter(diskAdapter);
        recyclerView.setLayoutManager(layoutManager);
        ItemDecorator mItemDecorator = new ItemDecorator(getResources()
                .getDimensionPixelOffset(R.dimen.disk_linear_space));
        recyclerView.addItemDecoration(mItemDecorator);

        diskAdapter.setPhotos(photoList);

        recyclerView.addOnScrollListener(new PaginationScrollListener(DiskClient.MAX_LIMIT) {
            @Override
            public void loadNext() {
                isLoading = true;
                mCurrentPage++;
                presenter.getPhotos(mCurrentPage);
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && floatingActionButton.isShown()) {
                    floatingActionButton.hide();
                } else if (dy < 0 && floatingActionButton.getVisibility() != View.INVISIBLE) {
                    floatingActionButton.show();
                }

                int topRowVerticalPosition =
                        recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0 && !isSelectable);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            updateDataSet();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        setEnabledSelection(isSelectable);
        updateToolbarTitle(String.valueOf(selectedPhotos.size()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setEnabledSelection(false);
                break;
            case R.id.menu_download:
                permissionManager.checkPermissions(
                        getActivity(),
                        new PermissionResultCallback() {
                            @Override
                            public void onPermissionGranted() {
                                presenter.downloadPhotos(selectedPhotos);
                                setEnabledSelection(false);
                            }

                            @Override
                            public void onPermissionDenied(@NotNull String[] permissionList) {

                            }

                            @Override
                            public void onPermissionPermanentlyDenied(@NotNull String permission) {

                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
                break;
            case R.id.menu_delete:
                presenter.deletePhotos(selectedPhotos);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setEnabledSelection(boolean enabled) {
        SelectableHelper.setMultipleSelection(enabled);

        int secondaryColor = enabled ? getResources().getColor(R.color.white) : getResources()
                .getColor(R.color.black);
//        actionBar.setDisplayHomeAsUpEnabled(enabled);
//        actionBar.setBackgroundDrawable(new ColorDrawable(enabled ? getResources()
//                .getColor(R.color.colorAccent)
//                : getResources().getColor(R.color.white)));
//        toolbar.setTitleTextColor(secondaryColor);
        setMenuIconsColor(menu, secondaryColor);

        swipeRefreshLayout.setEnabled(!enabled);

        isSelectable = enabled;
        diskAdapter.setSelection(enabled);
        menu.findItem(R.id.menu_delete).setVisible(enabled);
        menu.findItem(R.id.menu_download).setVisible(enabled);
        if (enabled) {
            floatingActionButton.hide();
        } else {
            floatingActionButton.show();
        }

        if (!enabled) {
            selectedPhotos.clear();
            updateToolbarTitle(getString(R.string.msg_disk));
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
                    diskAdapter.addUploadPhotos(mPhotos);
                    presenter.uploadPhotos(mPhotos);
                    break;
                case CAMERA_REQUEST:
                    if (cameraFileUri != null) {
                        Photo photo = getLocalPhoto(cameraFileUri);
                        if (photo != null) {
                            mPhotos.add(photo);
                            diskAdapter.addUploadPhotos(mPhotos);
                            presenter.uploadPhoto(photo);
                        }
                    }
                    break;
                case PhotoDetailActivity.DETAIL_REQUEST:
                    boolean isDataChanged =
                            data.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false);
                    if (isDataChanged) {
                        updateDataSet();
                    }
                    return;
            }
            uploadingList.addAll(mPhotos);

            resultIntent = null;
//            getStateView().hideStateView();
//            scrollToTop();
        }
    }

    private void updateDataSet() {
        mCurrentPage = 1;
        isLastPage = false;
        isLoading = true;
        photoList.clear();
        diskAdapter.clear();
        if (!uploadingList.isEmpty()) {
            diskAdapter.addUploadPhotos(uploadingList);
        }
        presenter.getPhotos(mCurrentPage);
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

    /**
     * Get local file path.
     */
    private Photo getLocalPhoto(Uri contentUri) {
        if (getActivity() != null) {
            String path = contentUri.toString();

            Cursor cursor =
                    getActivity().getContentResolver().query(contentUri, null, null, null, null);
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
            return photo;
        }
        return null;
    }

//    @Override
//    public void onInternetUnavailable() {
//        if (photoList.isEmpty()) {
//            getStateView().showEmptyView(
//                    R.drawable.ic_yandex_disk,
//                    getString(R.string.msg_yandex_failed_retrieve),
//                    getString(R.string.action_yandex_check_connection)
//            );
//        }
//    }

    @Override
    public void onUploadingPhotos(@NotNull List<? extends Photo> photos) {
        if (!photos.isEmpty()) {
            uploadingList.addAll(photos);
            diskAdapter.addUploadPhotos((List<Photo>) photos);
            presenter.uploadPhotos(photos);
        }
    }

    @Override
    public void onPhotosLoaded(@NotNull List<? extends Photo> photos) {
        swipeRefreshLayout.setRefreshing(false);
        if (!photos.isEmpty()) {
            floatingActionButton.show();
            photoList.addAll(photos);
            diskAdapter.addPhotos((List<Photo>) photos);
//            getStateView().hideStateView();
        } else {
            if (photoList.isEmpty()) {
//                getStateView().showEmptyView(
//                        R.drawable.ic_yandex_disk,
//                        getString(R.string.msg_yandex_ready),
//                        getString(R.string.action_yandex_add_items)
//                );
            }
            isLastPage = true;
        }
        isLoading = false;
    }

    @Override
    public void onPhotoUploaded(@NotNull Photo photo) {
        String uploadState = null;
        if (photo != null) {
            photoList.add(photo);
            diskAdapter.addPhoto(photo);
            uploadingList.remove(photo);
            diskAdapter.removeUploadedPhoto(photo);
        } else {
            uploadState = getString(R.string.msg_wait);
        }
        diskAdapter.changeUploadState(uploadState);
    }

    @Override
    public void onPhotoDownloaded(@NotNull String path) {
        Toast.makeText(
                getContext(),
                getString(R.string.msg_file_saved) + " " + path,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onPhotoDeleted(@NotNull Photo photo) {
        setEnabledSelection(false);
        photoList.remove(photo);
        diskAdapter.removePhoto(photo);
        Toast.makeText(
                getContext(),
                getString(R.string.msg_photo) + " "
                        + photo.getName() + " " + getString(R.string.msg_deleted).toLowerCase(),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

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
                    presenter.downloadPhotos(selectedPhotos);
                    setEnabledSelection(false);
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

    private void setBottomDialog() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            BottomDialogFragment bottomDialog = new BottomDialogFragment();
            bottomDialog.setTargetFragment(this, 0);
            bottomDialog.show(activity.getSupportFragmentManager(), BOTTOM);
        }
    }

    private void permissionDialog(String title, String message, int color) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            AlertDialogFragment alertDialog =
                    AlertDialogFragment.newInstance(title, message, color);
            alertDialog.setTargetFragment(this, 0);
            alertDialog.show(activity.getSupportFragmentManager(), ALERT);
        }
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
        final FragmentActivity activity = getActivity();
        if (activity == null) return;

        switch (id) {
            case R.id.container_camera:
                permissionManager.checkPermissions(
                        activity,
                        new PermissionResultCallback() {
                            @Override
                            public void onPermissionGranted() {
                                createCameraIntent();
                            }

                            @Override
                            public void onPermissionDenied(@NotNull String[] permissionList) {

                            }

                            @Override
                            public void onPermissionPermanentlyDenied(@NotNull String permission) {

                            }
                        },
                        Manifest.permission.CAMERA
                );
                break;
            case R.id.container_gallery:
                permissionManager.checkPermissions(
                        activity,
                        new PermissionResultCallback() {
                            @Override
                            public void onPermissionGranted() {
                                createGalleryIntent();
                            }

                            @Override
                            public void onPermissionDenied(@NotNull String[] permissionList) {

                            }

                            @Override
                            public void onPermissionPermanentlyDenied(@NotNull String permission) {

                            }
                        },
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
                break;
        }
    }

    private void createCameraIntent() {
        resultIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraFileUri = FileUtils.getInstance().getLocalFileUri();
        resultIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);
        startActivityForResult(resultIntent, CAMERA_REQUEST);
    }

    private void createGalleryIntent() {
        resultIntent =
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(
                Intent.createChooser(
                        resultIntent,
                        getString(R.string.msg_image_chooser)
                ),
                GALLERY_IMAGE_REQUEST
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
        recyclerView = null;
    }

    @Override
    public int layout() {
        return R.layout.fragment_disk;
    }
}