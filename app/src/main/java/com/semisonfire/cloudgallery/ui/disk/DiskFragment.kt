package com.semisonfire.cloudgallery.ui.disk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.LoadMoreListener
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.adapter.progress.ProgressItem
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.permisson.AlertButton
import com.semisonfire.cloudgallery.core.permisson.PermissionManager
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.databinding.FragmentDiskBinding
import com.semisonfire.cloudgallery.di.provider.provideComponent
import com.semisonfire.cloudgallery.ui.dialogs.BottomDialogFragment
import com.semisonfire.cloudgallery.ui.dialogs.DialogListener
import com.semisonfire.cloudgallery.ui.disk.adapter.DiskAdapter
import com.semisonfire.cloudgallery.ui.disk.di.DaggerDiskComponent
import com.semisonfire.cloudgallery.ui.disk.model.DiskViewModel
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.ui.selectable.SelectableFragment
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.longToast
import com.semisonfire.cloudgallery.utils.string
import javax.inject.Inject

class DiskFragment : SelectableFragment() {

    companion object {

        //Saved state constants
        private const val STATE_FILE_URI = "STATE_FILE_URI"

        //Dialog types
        private const val BOTTOM = "BOTTOM"
    }

    @Inject
    lateinit var presenter: DiskPresenter

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var adapter: DiskAdapter

    private var _viewBinding: FragmentDiskBinding? = null

    private val viewBinding: FragmentDiskBinding
        get() = _viewBinding!!

    //Uploading
    private val uploadingList: MutableList<Photo> = mutableListOf()

    //Camera/gallery request
    private var cameraFileUri: Uri? = null

    private val selectedPhotos = mutableListOf<Photo>()

    private var floatingActionButton: FloatingActionButton? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var diskModel: DiskViewModel

    private lateinit var pickFromCamera: ActivityResultLauncher<Intent>
    private lateinit var pickFromGallery: ActivityResultLauncher<Intent>
    private lateinit var openDetail: ActivityResultLauncher<Intent>

    override fun layout() = R.layout.fragment_disk
    override fun menuRes() = R.menu.menu_fragment

    override fun onAttach(context: Context) {

        pickFromCamera = registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = cameraFileUri
                if (uri != null) {
                    val photo = getLocalPhoto(uri)
                    if (photo != null) {
//                            diskAdapter.addUploadPhotos(photos)
                        presenter.uploadPhoto(photo)
                        uploadingList.add(photo)
                        viewBinding.rvDisk.scrollToPosition(0)
                    }
                }
            }
        }

        pickFromGallery = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val photos = extractFromGallery(it)
                    uploadingList.addAll(extractFromGallery(it))
//                    diskAdapter.addUploadPhotos(photos)
                    presenter.uploadPhotos(photos)
                    viewBinding.rvDisk.scrollToPosition(0)
                }
            }
        }

        openDetail = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isDataChanged =
                    result.data?.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false)
                        ?: false
                if (isDataChanged) {
//                    updateDataSet()
                }
            }
        }

        DaggerDiskComponent
            .factory()
            .create(
                context.provideComponent()
            )
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.getPhotos()

        if (savedInstanceState != null) {
            cameraFileUri = savedInstanceState.getParcelable(STATE_FILE_URI)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _viewBinding = FragmentDiskBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showContent(model: DiskViewModel) {
        this.diskModel = model
    }

    override fun onStart() {
        super.onStart()
        disposables.addAll(
            presenter
                .observeContent()
                .observeOn(foreground())
                .subscribe {
                    showContent(it)
                },
            presenter
                .observeDiskResult()
                .observeOn(foreground())
                .subscribe {
                    when (it) {
                        is DiskResult.Loaded -> onPhotosLoaded(it.photos)
                        is DiskResult.LoadMoreCompleted -> onLoadMoreComplete(it.photos)
                        is DiskResult.PhotoDeleted -> onPhotoDeleted(it.photo)
                        is DiskResult.PhotoDownloaded -> onPhotoDownloaded(it.path)
                        is DiskResult.PhotoUploaded -> onPhotoUploaded(it.photo, it.uploaded)
                        is DiskResult.Uploading -> onUploadingPhotos(it.photos)
                    }
                }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_FILE_URI, cameraFileUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    public override fun bind(view: View) {
        super.bind(view)
        activity?.let {
            floatingActionButton = it.findViewById(R.id.btn_add_new)
            swipeRefreshLayout = it.findViewById(R.id.swipe_refresh)
        }

        presenter.getUploadingPhotos()

        floatingActionButton?.show()
        floatingActionButton?.setOnClickListener { showBottomDialog() }

//        diskAdapter.setPhotoClickListener(object : SelectableHelper.OnPhotoListener {
//            override fun onPhotoClick(photos: List<Photo>, position: Int) {
//                if (context != null) {
//                    val intent = Intent(context, PhotoDetailActivity::class.java)
//                    intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position)
//                    intent.putParcelableArrayListExtra(
//                        PhotoDetailActivity.EXTRA_PHOTOS,
//                        diskModel.photoList as ArrayList<out Parcelable>
//                    )
//                    intent.putExtra(PhotoDetailActivity.EXTRA_FROM, PhotoDetailActivity.FROM_DISK)
//                    openDetail.launch(intent)
//                }
//            }
//
//            override fun onPhotoLongClick() {
//                setEnabledSelection(true)
//            }
//
//            override fun onSelectedPhotoClick(photo: Photo) {
//                if (photo.isSelected) {
//                    selectedPhotos.add(photo)
//                } else {
//                    selectedPhotos.remove(photo)
//                }
//                updateToolbarTitle(selectedPhotos.size.toString())
//                if (selectedPhotos.isEmpty()) {
//                    setEnabledSelection(false)
//                }
//            }
//        })

        adapter.loadMoreListener = object : LoadMoreListener {
            override fun loadMore(position: Int) {
                super.loadMore(position)
                presenter.loadMorePhotos()
            }
        }
        adapter.progressItem = ProgressItem()
        adapter.endlessScrollThreshold = 8

        viewBinding.rvDisk.layoutManager = LinearLayoutManager(context)
        viewBinding.rvDisk.adapter = adapter

        swipeRefreshLayout?.setOnRefreshListener {
            presenter.getPhotos()
            swipeRefreshLayout?.isRefreshing = true
//            updateDataSet()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> setEnabledSelection(false)
            R.id.menu_download -> {
                val activity = activity ?: return false

                val memoryPermission = object : PermissionResultCallback {
                    override fun onPermissionGranted() {
                        presenter.downloadPhotos(selectedPhotos)
                        setEnabledSelection(false)
                    }

                    override fun onPermissionDenied(permissionList: Array<String>) {
                        val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
                            permissionManager.checkPermissions(
                                activity,
                                this,
                                *permissionList
                            )
                        }

                        val action =
                            activity.string(R.string.action_download).lowercase()
                        val message =
                            "${activity.string(R.string.text_memory_rights_description)} $action"
                        showPermissionDialogWithCancelButton(activity, message, positiveButton)
                    }

                    override fun onPermissionPermanentlyDenied(permission: String) {
                        val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
                            permissionManager.openApplicationSettings(activity)
                        }

                        val action =
                            activity.string(R.string.action_download).lowercase()
                        val message =
                            "${activity.string(R.string.text_memory_rights_settings_description)} $action"
                        showPermissionDialogWithCancelButton(activity, message, positiveButton)
                    }
                }
                permissionManager.checkPermissions(
                    activity,
                    memoryPermission,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            R.id.menu_delete -> presenter.deletePhotos(selectedPhotos)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setEnabledSelection(enabled: Boolean) {
        super.setEnabledSelection(enabled)
        menu?.let {
            it.findItem(R.id.menu_delete)?.isVisible = enabled
            it.findItem(R.id.menu_download)?.isVisible = enabled
        }
//        diskAdapter.setSelection(enabled)

        swipeRefreshLayout?.isEnabled = !enabled
        floatingActionButton?.apply {
            if (enabled) hide() else show()
        }

        if (!enabled) {
            selectedPhotos.clear()
            updateToolbarTitle(getString(R.string.msg_disk))
        } else {
            updateToolbarTitle(selectedPhotos.size.toString())
        }
    }

//    private fun updateDataSet() {
//        diskAdapter.clear()
//        if (uploadingList.isNotEmpty()) {
//            diskAdapter.addUploadPhotos(uploadingList)
//        }
//    }

    private fun extractFromGallery(data: Intent): List<Photo> {
        val photos = mutableListOf<Photo>()
        if (data.clipData == null) {
            if (data.data == null) return emptyList()

            val imageUri = data.data ?: return emptyList()
            getLocalPhoto(imageUri)?.let {
                photos.add(it)
            }
            return emptyList()
        }

        val clipData = data.clipData ?: return emptyList()
        for (i in 0 until clipData.itemCount) {
            val imageUri = clipData.getItemAt(i).uri
            getLocalPhoto(imageUri)?.let {
                photos.add(it)
            }
        }

        return photos
    }

    /**
     * Get local file path.
     */
    private fun getLocalPhoto(contentUri: Uri): Photo? {
        val activity = activity ?: return null
        var path = contentUri.toString()

        val cursor = activity.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            if (idx != -1) {
                path = cursor.getString(idx)
                cursor.close()
            } else {
                path = FileUtils.getFile(contentUri).path
            }
        }

        val photo = Photo()
        photo.isUploaded = false
        photo.preview = contentUri.toString()
        photo.localPath = path
        photo.name = path.substring(path.lastIndexOf('/') + 1)
        return photo
    }

    private fun onUploadingPhotos(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            uploadingList.addAll(photos)
//            diskAdapter.addUploadPhotos(photos)
            presenter.uploadPhotos(photos)
        }
    }

    private fun onPhotosLoaded(photos: List<Item>) {
        swipeRefreshLayout?.isRefreshing = false
        if (photos.isNotEmpty()) {
            floatingActionButton?.show()
//            diskAdapter.setPhotos(photos)
            adapter.updateDataSet(photos)
        }
    }

    private fun onPhotoUploaded(photo: Photo, uploaded: Boolean) {
        var uploadState: String? = null
        if (uploaded) {
//            diskAdapter.addPhoto(photo)
            uploadingList.remove(photo)
//            diskAdapter.removeUploadedPhoto(photo)
        } else {
            uploadState = getString(R.string.msg_wait)
        }
//        diskAdapter.changeUploadState(uploadState)
    }

    private fun onPhotoDownloaded(path: String) {
        context?.longToast(getString(R.string.msg_file_saved) + " " + path)
    }

    private fun onPhotoDeleted(photo: Photo) {
        setEnabledSelection(false)
//        diskAdapter.removePhoto(photo)

        context?.let {
            val action = getString(R.string.msg_deleted).lowercase()
            it.longToast("${it.string(R.string.msg_photo)} ${photo.name} $action")
        }
    }

    private fun onLoadMoreComplete(items: List<Item>) {
//        adapter.addPhotos(items)
        adapter.onLoadMoreComplete(items)
        adapter.endlessScrollEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        activity?.let {
            permissionManager.onRequestPermissionsResult(
                it,
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    private fun showBottomDialog() {
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val bottomDialog = BottomDialogFragment()
            bottomDialog.dialogListener = object : DialogListener() {
                override fun onItemClick(view: View) {
                    when (view.id) {
                        R.id.container_camera -> permissionManager.checkPermissions(
                            activity,
                            object : PermissionResultCallback {
                                override fun onPermissionGranted() {
                                    createCameraIntent()
                                }

                                override fun onPermissionDenied(permissionList: Array<String>) {
                                    val positiveButton =
                                        AlertButton(activity.string(R.string.action_ok)) {
                                            permissionManager.checkPermissions(
                                                activity,
                                                this,
                                                *permissionList
                                            )
                                        }
                                    val action = activity.string(R.string.action_photo).lowercase()
                                    val message =
                                        "${activity.string(R.string.text_camera_rights_description)} $action"
                                    showPermissionDialogWithCancelButton(
                                        activity,
                                        message,
                                        positiveButton
                                    )
                                }

                                override fun onPermissionPermanentlyDenied(permission: String) {
                                    val positiveButton =
                                        AlertButton(activity.string(R.string.action_ok)) {
                                            permissionManager.openApplicationSettings(activity)
                                        }
                                    val action = activity.string(R.string.action_photo).lowercase()
                                    val message =
                                        "${activity.string(R.string.text_camera_rights_settings_description)} $action"
                                    showPermissionDialogWithCancelButton(
                                        activity,
                                        message,
                                        positiveButton
                                    )
                                }
                            },
                            Manifest.permission.CAMERA
                        )
                        R.id.container_gallery -> permissionManager.checkPermissions(
                            activity,
                            object : PermissionResultCallback {
                                override fun onPermissionGranted() {
                                    createGalleryIntent()
                                }

                                override fun onPermissionDenied(permissionList: Array<String>) {
                                    val positiveButton =
                                        AlertButton(activity.string(R.string.action_ok)) {
                                            permissionManager.checkPermissions(
                                                activity,
                                                this,
                                                *permissionList
                                            )
                                        }
                                    val action =
                                        activity.string(R.string.action_download).lowercase()
                                    val message =
                                        "${activity.string(R.string.text_memory_rights_description)} $action"
                                    showPermissionDialogWithCancelButton(
                                        activity,
                                        message,
                                        positiveButton
                                    )
                                }

                                override fun onPermissionPermanentlyDenied(permission: String) {
                                    val positiveButton =
                                        AlertButton(activity.string(R.string.action_ok)) {
                                            permissionManager.openApplicationSettings(activity)
                                        }
                                    val action =
                                        activity.string(R.string.action_download).lowercase()
                                    val message =
                                        "${activity.string(R.string.text_memory_rights_settings_description)} $action"
                                    showPermissionDialogWithCancelButton(
                                        activity,
                                        message,
                                        positiveButton
                                    )
                                }
                            },
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }
                }
            }
            bottomDialog.show(activity.supportFragmentManager, BOTTOM)
        }
    }

    private fun createCameraIntent() {
        cameraFileUri = FileUtils.localFileUri

        pickFromCamera.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri)
        )
    }

    private fun createGalleryIntent() {

        pickFromGallery.launch(
            Intent.createChooser(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true),
                requireContext().string(R.string.msg_image_chooser)
            )
        )
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
}