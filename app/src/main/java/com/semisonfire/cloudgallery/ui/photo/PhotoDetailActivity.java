package com.semisonfire.cloudgallery.ui.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.local.prefs.DiskPreferences;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.data.remote.RemoteDataSource;
import com.semisonfire.cloudgallery.data.remote.api.DiskClient;
import com.semisonfire.cloudgallery.ui.base.BaseActivity;
import com.semisonfire.cloudgallery.ui.main.dialogs.AlertDialogFragment;
import com.semisonfire.cloudgallery.utils.PermissionUtils;

import java.util.List;
import java.util.Locale;

public class PhotoDetailActivity extends BaseActivity implements PhotoDetailContract.View {

    private static final String TAG = PhotoDetailActivity.class.getSimpleName();

    private static final int MEMORY_REQUEST = 2000;
    private static final String ALERT = "ALERT";

    public static final int DETAIL_REQUEST = 432;

    //FROM TYPES
    public static final int FROM_DISK = 0;
    public static final int FROM_TRASH = 1;

    //EXTRAS
    public static final String EXTRA_CURRENT_PHOTO = "EXTRA_CURRENT_PHOTO";
    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";
    public static final String EXTRA_FROM = "EXTRA_FROM";

    private PhotoDetailPresenter<PhotoDetailContract.View> mPhotoDetailPresenter;
    private int mFrom;

    private PhotoDetailAdapter mAdapter;
    private List<Photo> mPhotoList;
    private Photo mCurrentPhoto;
    private int mCurrentPosition;

    //Orientation
    private int mOrientation;
    private boolean isDataChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpFullScreen();
        setContentView(R.layout.activity_photo_detail);

        RemoteDataSource remoteDataSource = new RemoteDataSource(DiskClient.getApi());

        //Device orientation state
        mOrientation = getResources().getConfiguration().orientation;

        //Create presenter
        mPhotoDetailPresenter = new PhotoDetailPresenter<>(new DiskPreferences(this), remoteDataSource);
        mPhotoDetailPresenter.attachView(this);

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentPosition = intent.getIntExtra(EXTRA_CURRENT_PHOTO, -1);
            mPhotoList = intent.getParcelableArrayListExtra(EXTRA_PHOTOS);
            mFrom = intent.getIntExtra(EXTRA_FROM, -1);
            bind();
        }
    }

    @Override
    public void bind() {
        setUpViewPager();
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        updateToolbarTitle(mCurrentPosition);
    }

    /** Make activity full screen. */
    private void setUpFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /** Create view pager. */
    private void setUpViewPager() {

        //View pager adapter
        mAdapter = new PhotoDetailAdapter();
        mAdapter.setOrientation(mOrientation);
        mAdapter.setItems(mPhotoList);

        //ViewPager
        ViewPager mViewPager = findViewById(R.id.vp_detailed_photos);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.photo_detail_space));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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

    /** Update toolbar title according to item position. */
    private void updateToolbarTitle(int position) {
        mCurrentPosition = position;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    String.format(Locale.getDefault(),
                            "%d %s %d",
                            position + 1, getString(R.string.msg_of), mPhotoList.size()));
        }
    }

    @Override
    public void onPhotoDownloaded(String path) {
        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
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
        isDataChanged = true;
        createReturnIntent();
        mPhotoList.remove(photo);
        if (mPhotoList.size() == 0) {
            finish();
            return;
        }
        mAdapter.setItems(mPhotoList);
        updateToolbarTitle(mCurrentPosition);
    }

    private void createReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("isChanged", isDataChanged);
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
        setMenuIconsColor(menu, R.color.white);
        return super.onCreateOptionsMenu(menu);
    }

    /** Change menu items icon color */
    private void setMenuIconsColor(Menu menu, @ColorRes int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mCurrentPhoto = mAdapter.getItemByPosition(mCurrentPosition);
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_share:
                mPhotoDetailPresenter.createShareFile(mAdapter.getCurrentItemBitmap());
                break;
            case R.id.menu_download:
                if (checkPermission(MEMORY_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    mPhotoDetailPresenter.download(mCurrentPhoto);
                }
                break;
            case R.id.menu_delete:
                mPhotoDetailPresenter.delete(mCurrentPhoto, mFrom);
                break;
            case R.id.menu_restore:
                mPhotoDetailPresenter.restore(mCurrentPhoto);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Check permission. */
    private boolean checkPermission(int request, String... permission) {
        if (!PermissionUtils.hasPermission(this, permission[0])) {
            PermissionUtils.requestPermissions(this, permission, request);
            return false;
        }
        return true;
    }

    /** Permission alert dialog. */
    private void permissionDialog(String title, String message, int color) {
        AlertDialogFragment mAlertDialog = AlertDialogFragment.newInstance(title, message, color);
        mAlertDialog.show(getSupportFragmentManager(), ALERT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        int color = getResources().getColor(R.color.colorAccent);
        String title = getString(R.string.msg_request_permission);
        String body = getString(R.string.msg_memory_permission);

        switch (requestCode) {
            case MEMORY_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoDetailPresenter.download(mCurrentPhoto);
                } else {
                    if (!PermissionUtils.shouldShowRational(this, permissions[0])) {
                        permissionDialog(title, body, color);
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mPhotoList.clear();
        if (mPhotoDetailPresenter != null) {
            mPhotoDetailPresenter.dispose();
        }
        super.onDestroy();
    }
}
