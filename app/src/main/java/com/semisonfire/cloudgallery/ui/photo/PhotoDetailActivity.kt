package com.semisonfire.cloudgallery.ui.photo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.permisson.AlertButton
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.core.ui.BaseActivity
import com.semisonfire.cloudgallery.di.api.NavigationComponentApi
import com.semisonfire.cloudgallery.ui.di.ComponentProvider
import com.semisonfire.cloudgallery.ui.di.provideComponent
import com.semisonfire.cloudgallery.ui.photo.di.DaggerPhotoDetailComponent
import com.semisonfire.cloudgallery.ui.photo.di.PhotoDetailComponent
import com.semisonfire.cloudgallery.ui.photo.model.PhotoDetailViewModel
import com.semisonfire.cloudgallery.utils.dimen
import com.semisonfire.cloudgallery.utils.longToast
import com.semisonfire.cloudgallery.utils.setMenuIconsColor
import com.semisonfire.cloudgallery.utils.string
import java.util.*

interface PhotoDetailView : MvpView<PhotoDetailViewModel> {

    fun onPhotoDownloaded(path: String)
    fun onFilePrepared(uri: Uri)
    fun onFilesChanged(photo: Photo)
}

class PhotoDetailActivity :
    BaseActivity<PhotoDetailViewModel, PhotoDetailView, PhotoDetailPresenter>(),
    PhotoDetailView,
    ComponentProvider<NavigationComponentApi> {

    var component: PhotoDetailComponent? = null

    private val adapter = PhotoDetailAdapter()
    private var photoList = mutableListOf<Photo>()

    private var currentPosition = 0
    private var from = 0

    //Orientation
    private var orientation = 0

    override fun component(): NavigationComponentApi? {
        return component
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        component = DaggerPhotoDetailComponent
            .factory()
            .create(
                this,
                provideComponent()
            )
        component?.inject(this)

        setUpFullScreen()
        super.onCreate(savedInstanceState)
        //Device orientation state
        orientation = resources.configuration.orientation
        val intent = intent
        when {
            intent != null -> {
                currentPosition = intent.getIntExtra(EXTRA_CURRENT_PHOTO, -1)
                photoList = intent.getParcelableArrayListExtra(EXTRA_PHOTOS) ?: mutableListOf()
                from = intent.getIntExtra(EXTRA_FROM, -1)
            }
            savedInstanceState != null -> {
                currentPosition = savedInstanceState.getInt(
                    STATE_CURRENT_POS,
                    -1
                )
                photoList =
                    savedInstanceState.getParcelableArrayList(STATE_PHOTO_LIST) ?: mutableListOf()
                from = savedInstanceState.getInt(EXTRA_FROM, -1)
            }
        }
    }

    public override fun bind() {
        super.bind()
        setUpViewPager()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateToolbarTitle(currentPosition)
    }

    /**
     * Make activity full screen.
     */
    private fun setUpFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    /**
     * Create view pager.
     */
    private fun setUpViewPager() {
        adapter.setOrientation(orientation)
        adapter.setItems(photoList)

        //ViewPager
        val viewPager = findViewById<ViewPager>(R.id.vp_detailed_photos)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(currentPosition, false)
        viewPager.pageMargin = dimen(R.dimen.photo_detail_space)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateToolbarTitle(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        //Update toolbar
        updateToolbarTitle(currentPosition)
    }

    /**
     * Update toolbar title according to item position.
     */
    private fun updateToolbarTitle(position: Int) {
        currentPosition = position
        supportActionBar?.title = "${position + 1} ${string(R.string.msg_of)} ${photoList.size}"
    }

    override fun onPhotoDownloaded(path: String) {
        longToast("${string(R.string.msg_file_saved)} $path")
    }

    override fun onFilePrepared(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpg"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.msg_share_chooser)))
    }

    override fun onFilesChanged(photo: Photo) {
        createReturnIntent(true)
        photoList.remove(photo)
        if (photoList.isEmpty()) {
            finish()
            return
        }

        adapter.setItems(photoList)
        updateToolbarTitle(currentPosition)
    }

    private fun createReturnIntent(isDataChanged: Boolean) {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_CHANGED, isDataChanged)
        setResult(Activity.RESULT_OK, returnIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photo, menu)
        val share = menu.findItem(R.id.menu_share)
        val download = menu.findItem(R.id.menu_download)
        val restore = menu.findItem(R.id.menu_restore)
        when (from) {
            FROM_DISK -> {
                share.isVisible = true
                download.isVisible = true
                restore.isVisible = false
            }
            FROM_TRASH -> {
                share.isVisible = false
                download.isVisible = false
                restore.isVisible = true
            }
        }
        setMenuIconsColor(menu, resources.getColor(R.color.white))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentPhoto = adapter.getItemByPosition(currentPosition) ?: return false
        when (item.itemId) {
            android.R.id.home -> super.onBackPressed()
            R.id.menu_share -> {
                val bitmap = adapter.currentItemBitmap ?: return false
                presenter.createShareFile(bitmap)
            }
            R.id.menu_download -> {
                permissionManager.checkPermissions(
                    this,
                    object : PermissionResultCallback {
                        override fun onPermissionGranted() {
                            presenter.download(currentPhoto)
                        }

                        override fun onPermissionDenied(permissionList: Array<String>) {
                            val positiveButton = AlertButton(string(R.string.action_ok)) {
                                permissionManager.checkPermissions(
                                    this@PhotoDetailActivity,
                                    this,
                                    *permissionList
                                )
                            }
                            val action = string(R.string.action_download).toLowerCase(Locale.ROOT)
                            val message =
                                "${string(R.string.text_memory_rights_description)} $action"
                            showPermissionDialogWithCancelButton(
                                this@PhotoDetailActivity,
                                message,
                                positiveButton
                            )
                        }

                        override fun onPermissionPermanentlyDenied(permission: String) {
                            val positiveButton = AlertButton(string(R.string.action_ok)) {
                                permissionManager.openApplicationSettings(this@PhotoDetailActivity)
                            }
                            val action = string(R.string.action_download).toLowerCase(Locale.ROOT)
                            val message =
                                "${string(R.string.text_memory_rights_settings_description)} $action"
                            showPermissionDialogWithCancelButton(
                                this@PhotoDetailActivity,
                                message,
                                positiveButton
                            )
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            R.id.menu_delete -> presenter.delete(currentPhoto, from)
            R.id.menu_restore -> presenter.restore(currentPhoto)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showPermissionDialogWithCancelButton(
        activity: FragmentActivity,
        message: String,
        positiveButton: AlertButton
    ) {
        val negativeButton = AlertButton(activity.string(R.string.action_cancel))
        permissionManager.permissionAlertDialog(
            context = activity,
            title = activity.string(R.string.text_request_rights),
            message = message,
            positive = positiveButton,
            negative = negativeButton
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_CURRENT_POS, currentPosition)
        outState.putInt(STATE_FROM, from)
        outState.putParcelableArrayList(
            STATE_PHOTO_LIST,
            photoList as ArrayList<out Parcelable>
        )
    }

    override fun layout(): Int {
        return R.layout.activity_photo_detail
    }

    companion object {
        const val DETAIL_REQUEST = 432

        //FROM TYPES
        const val FROM_DISK = 0
        const val FROM_TRASH = 1

        //STATE
        private const val STATE_CURRENT_POS = "STATE_CURRENT_POS"
        private const val STATE_PHOTO_LIST = "STATE_PHOTO_LIST"
        private const val STATE_FROM = "STATE_PHOTO_LIST"

        //EXTRAS
        const val EXTRA_CHANGED = "EXTRA_CHANGED"
        const val EXTRA_CURRENT_PHOTO = "EXTRA_CURRENT_PHOTO"
        const val EXTRA_PHOTOS = "EXTRA_PHOTOS"
        const val EXTRA_FROM = "EXTRA_FROM"
    }
}