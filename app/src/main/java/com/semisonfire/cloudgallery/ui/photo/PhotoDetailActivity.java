package com.semisonfire.cloudgallery.ui.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.core.ui.BaseActivity;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.dialogs.AlertDialogFragment;
import com.semisonfire.cloudgallery.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.semisonfire.cloudgallery.utils.ColorUtilsKt.setMenuIconsColor;

public class PhotoDetailActivity extends
        BaseActivity<PhotoDetailContract.View, PhotoDetailContract.Presenter> implements
        PhotoDetailContract.View {

    private static final String TAG = PhotoDetailActivity.class.getSimpleName();

    private static final int MEMORY_REQUEST = 2000;
    private static final String ALERT = "ALERT";

    public static final int DETAIL_REQUEST = 432;

    //FROM TYPES
    public static final int FROM_DISK = 0;
    public static final int FROM_TRASH = 1;

    //STATE
    private static final String STATE_CURRENT_POS = "STATE_CURRENT_POS";
    private static final String STATE_PHOTO_LIST = "STATE_PHOTO_LIST";
    private static final String STATE_FROM = "STATE_PHOTO_LIST";

    //EXTRAS
    public static final String EXTRA_CHANGED = "EXTRA_CHANGED";
    public static final String EXTRA_CURRENT_PHOTO = "EXTRA_CURRENT_PHOTO";
    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";
    public static final String EXTRA_FROM = "EXTRA_FROM";

    private int mFrom;

    private PhotoDetailAdapter mAdapter;
    private List<Photo> mPhotoList;
    private Photo mCurrentPhoto;
    private int mCurrentPosition;

    //Orientation
    private int mOrientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setUpFullScreen();
        super.onCreate(savedInstanceState);


        //Device orientation state
        mOrientation = getResources().getConfiguration().orientation;

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentPosition = intent.getIntExtra(EXTRA_CURRENT_PHOTO, -1);
            mPhotoList = intent.getParcelableArrayListExtra(EXTRA_PHOTOS);
            mFrom = intent.getIntExtra(EXTRA_FROM, -1);
        } else if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POS, -1);
            mPhotoList = savedInstanceState.getParcelableArrayList(STATE_PHOTO_LIST);
            mFrom = savedInstanceState.getInt(EXTRA_FROM, -1);
        }
    }

    @Override
    public void bind() {
        super.bind();
        setUpViewPager();
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        updateToolbarTitle(mCurrentPosition);
    }

    /**
     * Make activity full screen.
     */
    private void setUpFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    /**
     * Create view pager.
     */
    private void setUpViewPager() {

        //View pager adapter
        mAdapter = new PhotoDetailAdapter();
        mAdapter.setOrientation(mOrientation);
        mAdapter.setItems(mPhotoList);

        //ViewPager
        ViewPager mViewPager = findViewById(R.id.vp_detailed_photos);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);
        mViewPager
                .setPageMargin(getResources().getDimensionPixelOffset(R.dimen.photo_detail_space));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position,
                    float positionOffset,
                    int positionOffsetPixels
            ) {
            }

            @Override
            public void onPageSelected(int position) {
                updateToolbarTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //Update toolbar
        updateToolbarTitle(mCurrentPosition);
    }

    /**
     * Update toolbar title according to item position.
     */
    private void updateToolbarTitle(int position) {
        mCurrentPosition = position;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    String.format(Locale.getDefault(),
                            "%d %s %d",
                            position + 1, getString(R.string.msg_of), mPhotoList.size()
                    ));
        }
    }

    @Override
    public void onPhotoDownloaded(String path) {
        Toast.makeText(this, getString(R.string.msg_file_saved) + " " + path, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onFilePrepared(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.msg_share_chooser)));
    }

    @Override
    public void onFilesChanged(Photo photo) {
        createReturnIntent(true);
        mPhotoList.remove(photo);
        if (mPhotoList.size() == 0) {
            finish();
            return;
        }
        mAdapter.setItems(mPhotoList);
        updateToolbarTitle(mCurrentPosition);
    }

    private void createReturnIntent(boolean isDataChanged) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_CHANGED, isDataChanged);
        setResult(Activity.RESULT_OK, returnIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        MenuItem share = menu.findItem(R.id.menu_share);
        MenuItem download = menu.findItem(R.id.menu_download);
        MenuItem restore = menu.findItem(R.id.menu_restore);
        switch (mFrom) {
            case FROM_DISK:
                share.setVisible(true);
                download.setVisible(true);
                restore.setVisible(false);
                break;
            case FROM_TRASH:
                share.setVisible(false);
                download.setVisible(false);
                restore.setVisible(true);
                break;
        }
        setMenuIconsColor(menu, getResources().getColor(R.color.white));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mCurrentPhoto = mAdapter.getItemByPosition(mCurrentPosition);
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_share:
                presenter.createShareFile(mAdapter.getCurrentItemBitmap());
                break;
            case R.id.menu_download:
                if (checkPermission(
                        MEMORY_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {
                    presenter.download(mCurrentPhoto);
                }
                break;
            case R.id.menu_delete:
                presenter.delete(mCurrentPhoto, mFrom);
                break;
            case R.id.menu_restore:
                presenter.restore(mCurrentPhoto);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check permission.
     */
    private boolean checkPermission(int request, String... permission) {
        if (!PermissionUtils.hasPermission(this, permission[0])) {
            PermissionUtils.requestPermissions(this, permission, request);
            return false;
        }
        return true;
    }

    /**
     * Permission alert dialog.
     */
    private void permissionDialog(String title, String message, int color) {
        AlertDialogFragment mAlertDialog = AlertDialogFragment.newInstance(title, message, color);
        mAlertDialog.show(getSupportFragmentManager(), ALERT);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        int color = getResources().getColor(R.color.colorAccent);
        String title = getString(R.string.msg_request_permission);
        String body = getString(R.string.msg_memory_permission);

        if (requestCode == MEMORY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.download(mCurrentPhoto);
            } else {
                if (!PermissionUtils.shouldShowRational(this, permissions[0])) {
                    permissionDialog(title, body, color);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POS, mCurrentPosition);
        outState.putInt(STATE_FROM, mFrom);
        outState.putParcelableArrayList(
                STATE_PHOTO_LIST,
                (ArrayList<? extends Parcelable>) mPhotoList
        );
    }

    @Override
    public int layout() {
        return R.layout.activity_photo_detail;
    }
}
