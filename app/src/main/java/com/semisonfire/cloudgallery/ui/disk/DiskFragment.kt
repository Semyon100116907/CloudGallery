package com.semisonfire.cloudgallery.ui.disk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.adapter.LoadMoreListener
import com.semisonfire.cloudgallery.adapter.progress.ProgressItem
import com.semisonfire.cloudgallery.common.scroll.HorizontalScrollItem
import com.semisonfire.cloudgallery.core.permisson.AlertButton
import com.semisonfire.cloudgallery.core.permisson.PermissionManager
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.core.ui.ContentFragment
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.databinding.FragmentDiskBinding
import com.semisonfire.cloudgallery.di.provider.provideComponent
import com.semisonfire.cloudgallery.ui.dialogs.BottomDialogFragment
import com.semisonfire.cloudgallery.ui.dialogs.DialogListener
import com.semisonfire.cloudgallery.ui.disk.adapter.DiskAdapter
import com.semisonfire.cloudgallery.ui.disk.di.DaggerDiskComponent
import com.semisonfire.cloudgallery.ui.disk.model.DiskViewModel
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.utils.FileUtils
import com.semisonfire.cloudgallery.utils.foreground
import com.semisonfire.cloudgallery.utils.longToast
import com.semisonfire.cloudgallery.utils.string
import java.io.File
import java.util.UUID
import javax.inject.Inject

class DiskFragment : ContentFragment() {

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

    private var floatingActionButton: FloatingActionButton? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    // Activity Result
    private var cameraFileUri: Uri? = null
    private lateinit var pickFromCamera: ActivityResultLauncher<Intent>
    private lateinit var pickFromGallery: ActivityResultLauncher<Intent>
    private lateinit var openDetail: ActivityResultLauncher<Intent>

    override fun layout() = R.layout.fragment_disk

    override fun onAttach(context: Context) {

        pickFromCamera = registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = cameraFileUri
                if (uri != null) {
                    val photo = getLocalPhoto(uri)
                    if (photo != null) {
                        presenter.uploadPhoto(photo)
                        viewBinding.rvDisk.scrollToPosition(0)
                    }
                }
            }
        }

        pickFromGallery = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    presenter.uploadPhotos(extractFromGallery(it))
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
            .create(context.provideComponent())
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.getPhotos()
        presenter.getUploadingPhotos()

        if (savedInstanceState != null) {
            cameraFileUri = savedInstanceState.getParcelable(STATE_FILE_URI)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _viewBinding = FragmentDiskBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showContent(model: DiskViewModel) {
        adapter.updateDataSet(model.getListItems())
        adapter.endlessScrollEnabled = model.hasMore.get()
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
                        is DiskResult.Update -> {
                            adapter.updateDataSet(it.photos)
                        }
                        is DiskResult.Loaded -> onPhotosLoaded(it)
                        is DiskResult.LoadMoreCompleted -> onLoadMoreComplete(it)
                        is DiskResult.PhotoDeleted -> onPhotoDeleted(it.photo)
                        is DiskResult.PhotoDownloaded -> onPhotoDownloaded(it.path)
                        is DiskResult.PhotoUploaded -> onPhotoUploaded(it.photo, it.uploaded)
                        is DiskResult.Uploading -> onUploadingPhotos(it)
                    }
                }
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_FILE_URI, cameraFileUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dispose()
    }

    public override fun bind(view: View) {
        super.bind(view)
        updateToolbarTitle(getString(R.string.msg_disk))

        activity?.let {
            floatingActionButton = it.findViewById(R.id.btn_add_new)
            swipeRefreshLayout = it.findViewById(R.id.swipe_refresh)
        }

        floatingActionButton?.show()
        floatingActionButton?.setOnClickListener { showBottomDialog() }

        adapter.loadMoreListener = object : LoadMoreListener {
            override fun loadMore(position: Int) {
                super.loadMore(position)
                presenter.loadMorePhotos()
            }
        }
        adapter.progressItem = ProgressItem()
        adapter.endlessScrollThreshold = 8

        viewBinding.rvDisk.itemAnimator = DefaultItemAnimator().apply { this.changeDuration = 0 }
        viewBinding.rvDisk.layoutManager = LinearLayoutManager(context)
        viewBinding.rvDisk.adapter = adapter

        swipeRefreshLayout?.setOnRefreshListener {
            presenter.getPhotos()
            adapter.endlessScrollEnabled = true
            swipeRefreshLayout?.isRefreshing = true
        }
    }

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

        val uuid = UUID.randomUUID().toString()
        val file = File(path)
        val name = file.nameWithoutExtension
        val extension = file.extension

        return Photo(
            id = uuid,
            preview = contentUri.toString(),
            localPath = path,
            name = "$name-$uuid.$extension"
        )
    }

    private fun onUploadingPhotos(result: DiskResult.Uploading) {

        val uploading = result.uploading
        if (uploading.items.isEmpty()) {
            adapter.removeIf {
                it is HorizontalScrollItem<*> && it.id == uploading.id
            }
        } else {
            adapter.insertOrUpdate(uploading, 0)
            viewBinding.rvDisk.scrollToPosition(0)
        }
    }

    private fun onPhotosLoaded(result: DiskResult.Loaded) {
        swipeRefreshLayout?.isRefreshing = false

        val photos = result.photos
        floatingActionButton?.show()
        adapter.updateDataSet(photos)
        adapter.endlessScrollEnabled = result.hasMore
    }

    private fun onPhotoUploaded(photo: Photo, uploaded: Boolean) {
//        if (!uploaded) {
//            requireContext().shortToast(getString(R.string.msg_wait))
//        }
    }

    private fun onPhotoDownloaded(path: String) {
        context?.longToast(getString(R.string.msg_file_saved) + " " + path)
    }

    private fun onPhotoDeleted(photo: Photo) {
//        diskAdapter.removePhoto(photo)

        context?.let {
            val action = getString(R.string.msg_deleted).lowercase()
            it.longToast("${it.string(R.string.msg_photo)} ${photo.name} $action")
        }
    }

    private fun onLoadMoreComplete(result: DiskResult.LoadMoreCompleted) {
        adapter.onLoadMoreComplete(emptyList())
        adapter.updateDataSet(result.photos)
        adapter.endlessScrollEnabled = result.hasMore
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
        val context = requireContext()
        val bottomDialog = BottomDialogFragment()
        bottomDialog.dialogListener = object : DialogListener() {
            override fun onItemClick(view: View) {
                when (view.id) {
                    R.id.container_camera -> permissionManager.checkPermissions(
                        this@DiskFragment,
                        object : PermissionResultCallback {
                            override fun onPermissionGranted() {
                                createCameraIntent()
                            }

                            override fun onPermissionDenied(permissionList: Array<String>) {
                                val positiveButton =
                                    AlertButton(context.string(R.string.action_ok)) {
                                        permissionManager.checkPermissions(
                                            this@DiskFragment,
                                            this,
                                            *permissionList
                                        )
                                    }
                                val action = context.string(R.string.action_photo).lowercase()
                                val message =
                                    "${context.string(R.string.text_camera_rights_description)} $action"
                                showPermissionDialogWithCancelButton(
                                    context,
                                    message,
                                    positiveButton
                                )
                            }

                            override fun onPermissionPermanentlyDenied(permission: String) {
                                val positiveButton =
                                    AlertButton(context.string(R.string.action_ok)) {
                                        permissionManager.openApplicationSettings(context)
                                    }
                                val action = context.string(R.string.action_photo).lowercase()
                                val message =
                                    "${context.string(R.string.text_camera_rights_settings_description)} $action"
                                showPermissionDialogWithCancelButton(
                                    context,
                                    message,
                                    positiveButton
                                )
                            }
                        },
                        Manifest.permission.CAMERA
                    )
                    R.id.container_gallery -> permissionManager.checkPermissions(
                        this@DiskFragment,
                        object : PermissionResultCallback {
                            override fun onPermissionGranted() {
                                createGalleryIntent()
                            }

                            override fun onPermissionDenied(permissionList: Array<String>) {
                                val positiveButton =
                                    AlertButton(context.string(R.string.action_ok)) {
                                        permissionManager.checkPermissions(
                                            this@DiskFragment,
                                            this,
                                            *permissionList
                                        )
                                    }
                                val action =
                                    context.string(R.string.action_download).lowercase()
                                val message =
                                    "${context.string(R.string.text_memory_rights_description)} $action"
                                showPermissionDialogWithCancelButton(
                                    context,
                                    message,
                                    positiveButton
                                )
                            }

                            override fun onPermissionPermanentlyDenied(permission: String) {
                                val positiveButton =
                                    AlertButton(context.string(R.string.action_ok)) {
                                        permissionManager.openApplicationSettings(context)
                                    }
                                val action =
                                    context.string(R.string.action_download).lowercase()
                                val message =
                                    "${context.string(R.string.text_memory_rights_settings_description)} $action"
                                showPermissionDialogWithCancelButton(
                                    context,
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
        bottomDialog.show(requireActivity().supportFragmentManager, BOTTOM)
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
        context: Context,
        message: String,
        positiveButton: AlertButton
    ) {
        val negativeButton = AlertButton(context.string(R.string.action_cancel))
        permissionManager.permissionAlertDialog(
            context = context,
            title = context.string(R.string.text_request_rights),
            message = message,
            positive = positiveButton,
            negative = negativeButton
        )
    }
}