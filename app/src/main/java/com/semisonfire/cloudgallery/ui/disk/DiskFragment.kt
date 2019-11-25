package com.semisonfire.cloudgallery.ui.disk

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.TextView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.permisson.AlertButton
import com.semisonfire.cloudgallery.core.permisson.PermissionResultCallback
import com.semisonfire.cloudgallery.core.ui.BaseFragment
import com.semisonfire.cloudgallery.core.ui.state.State
import com.semisonfire.cloudgallery.core.ui.state.StateViewDelegate
import com.semisonfire.cloudgallery.core.ui.state.strategy.EnterActionStrategy
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.ui.dialogs.BottomDialogFragment
import com.semisonfire.cloudgallery.ui.dialogs.base.BottomDialogListener
import com.semisonfire.cloudgallery.ui.disk.adapter.DiskAdapter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.utils.*
import java.util.*

class DiskFragment : BaseFragment<DiskContract.View, DiskContract.Presenter>(), DiskContract.View,
  BottomDialogListener {

  //RecyclerView
  private var recyclerView: RecyclerView? = null
  private val diskAdapter: DiskAdapter = DiskAdapter()
  private val photoList: MutableList<Photo> = mutableListOf()

  private var menu: Menu? = null

  //Uploading
  private val uploadingList: MutableList<Photo> = mutableListOf()

  //Camera/gallery request
  private var cameraFileUri: Uri? = null
  private var isSelectable: Boolean = false

  private val selectedPhotos = mutableListOf<Photo>()

  private var floatingActionButton: FloatingActionButton? = null
  private var swipeRefreshLayout: SwipeRefreshLayout? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (savedInstanceState != null) {
      cameraFileUri = savedInstanceState.getParcelable(STATE_FILE_URI)
      isSelectable = savedInstanceState.getBoolean(STATE_SELECTABLE)

      setSelectableItems()
    }
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    presenter.getPhotos(0)
  }

  private fun setSelectableItems() {
    for (photo in photoList) {
      if (photo.isSelected) {
        selectedPhotos.add(photo)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(STATE_FILE_URI, cameraFileUri)
    outState.putBoolean(STATE_SELECTABLE, isSelectable)
  }

  public override fun bind(view: View) {
    super.bind(view)
    activity?.let {
      floatingActionButton = it.findViewById(R.id.btn_add_new)
      swipeRefreshLayout = it.findViewById(R.id.swipe_refresh)
    }
    recyclerView = view.findViewById(R.id.rv_disk)
    presenter.getUploadingPhotos()

    floatingActionButton?.show()
    floatingActionButton?.setOnClickListener { setBottomDialog() }

    diskAdapter.setPhotoClickListener(object : SelectableHelper.OnPhotoListener {
      override fun onPhotoClick(photos: List<Photo>, position: Int) {
        if (context != null) {
          val intent = Intent(context, PhotoDetailActivity::class.java)
          intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position)
          intent.putParcelableArrayListExtra(
            PhotoDetailActivity.EXTRA_PHOTOS,
            photoList as ArrayList<out Parcelable>
          )
          intent.putExtra(PhotoDetailActivity.EXTRA_FROM, PhotoDetailActivity.FROM_DISK)
          startActivityForResult(intent, PhotoDetailActivity.DETAIL_REQUEST)
        }
      }

      override fun onPhotoLongClick() {
        setEnabledSelection(true)
      }

      override fun onSelectedPhotoClick(photo: Photo) {
        if (photo.isSelected) {
          selectedPhotos.add(photo)
        } else {
          selectedPhotos.remove(photo)
        }
        updateToolbarTitle(selectedPhotos.size.toString())
        if (selectedPhotos.isEmpty()) {
          setEnabledSelection(false)
        }
      }
    })
    val layoutManager = LinearLayoutManager(context)

    recyclerView?.adapter = diskAdapter
    recyclerView?.layoutManager = layoutManager
    val mItemDecorator = ItemDecorator(
      resources
        .getDimensionPixelOffset(R.dimen.disk_linear_space)
    )
    recyclerView?.addItemDecoration(mItemDecorator)

    diskAdapter.setPhotos(photoList)

    swipeRefreshLayout?.setOnRefreshListener {
      presenter.getPhotos(0)
      swipeRefreshLayout?.isRefreshing = true
      updateDataSet()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
    super.onCreateOptionsMenu(menu, inflater)
    this.menu = menu
    setEnabledSelection(isSelectable)
    updateToolbarTitle(selectedPhotos.size.toString())
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

            val action = activity.string(R.string.action_download).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_memory_rights_description)} $action"
            showPermissionDialogWithCancelButton(activity, message, positiveButton)
          }

          override fun onPermissionPermanentlyDenied(permission: String) {
            val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
              permissionManager.openApplicationSettings(activity)
            }

            val action = activity.string(R.string.action_download).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_memory_rights_settings_description)} $action"
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

  fun setEnabledSelection(enabled: Boolean) {
    SelectableHelper.setMultipleSelection(enabled)

    val secondaryColorRes = if (enabled) R.color.white else R.color.black
    val secondaryColor = context?.color(secondaryColorRes) ?: Color.WHITE

    activity?.let {
      if (it is AppCompatActivity) {
        it.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
        it.supportActionBar?.setBackgroundDrawable(it.colorResDrawable(if (enabled) R.color.colorAccent else R.color.white))

        it.findViewById<Toolbar>(R.id.toolbar).setTitleTextColor(secondaryColor)
      }
    }

    menu?.let {
      setMenuIconsColor(it, secondaryColor)
      it.findItem(R.id.menu_delete)?.isVisible = enabled
      it.findItem(R.id.menu_download)?.isVisible = enabled
    }

    swipeRefreshLayout?.isEnabled = !enabled

    isSelectable = enabled
    diskAdapter.setSelection(enabled)

    floatingActionButton?.apply {
      if (enabled) hide() else show()
    }

    if (!enabled) {
      selectedPhotos.clear()
      updateToolbarTitle(getString(R.string.msg_disk))
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      val photos = mutableListOf<Photo>()
      when (requestCode) {
        GALLERY_IMAGE_REQUEST -> {
          data?.let {
            extractFromGallery(data, photos)
            diskAdapter.addUploadPhotos(photos)
            presenter.uploadPhotos(photos)
          }
        }
        CAMERA_REQUEST -> {
          val uri = cameraFileUri
          if (uri != null) {
            val photo = getLocalPhoto(uri)
            if (photo != null) {
              photos.add(photo)
              diskAdapter.addUploadPhotos(photos)
              presenter.uploadPhoto(photo)
            }
          }
        }
        PhotoDetailActivity.DETAIL_REQUEST -> {
          val isDataChanged = data?.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false)
            ?: return
          if (isDataChanged) {
            updateDataSet()
          }
          return
        }
      }
      uploadingList.addAll(photos)

      recyclerView?.scrollToPosition(0)
    }
  }

  private fun updateDataSet() {
    photoList.clear()
    diskAdapter.clear()
    if (uploadingList.isNotEmpty()) {
      diskAdapter.addUploadPhotos(uploadingList)
    }
  }

  private fun extractFromGallery(data: Intent, photos: MutableList<Photo>) {
    if (data.clipData == null) {
      if (data.data == null) return

      val imageUri = data.data ?: return
      getLocalPhoto(imageUri)?.let {
        photos.add(it)
      }
      return
    }

    val clipData = data.clipData ?: return
    for (i in 0 until clipData.itemCount) {
      val imageUri = clipData.getItemAt(i).uri
      getLocalPhoto(imageUri)?.let {
        photos.add(it)
      }
    }
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
        path = FileUtils.getInstance().getFile(contentUri).path
      }
    }

    val photo = Photo()
    photo.isUploaded = false
    photo.preview = contentUri.toString()
    photo.localPath = path
    photo.name = path.substring(path.lastIndexOf('/') + 1)
    return photo
  }

  override fun onUploadingPhotos(photos: List<Photo>) {
    if (photos.isNotEmpty()) {
      uploadingList.addAll(photos)
      diskAdapter.addUploadPhotos(photos)
      presenter.uploadPhotos(photos)
    }
  }

  override fun onPhotosLoaded(photos: List<Photo>) {
    swipeRefreshLayout?.isRefreshing = false
    if (photos.isNotEmpty()) {
      floatingActionButton?.show()
      photoList.addAll(photos)
      diskAdapter.addPhotos(photos)
    } else {
      if (photoList.isEmpty()) {
      }
    }
  }

  override fun onPhotoUploaded(photo: Photo, uploaded: Boolean) {
    var uploadState: String? = null
    if (uploaded) {
      photoList.add(photo)
      diskAdapter.addPhoto(photo)
      uploadingList.remove(photo)
      diskAdapter.removeUploadedPhoto(photo)
    } else {
      uploadState = getString(R.string.msg_wait)
    }
    diskAdapter.changeUploadState(uploadState)
  }

  override fun onPhotoDownloaded(path: String) {
    context?.longToast(getString(R.string.msg_file_saved) + " " + path)
  }

  override fun onPhotoDeleted(photo: Photo) {
    setEnabledSelection(false)
    photoList.remove(photo)
    diskAdapter.removePhoto(photo)

    context?.let {
      val action = getString(R.string.msg_deleted).toLowerCase(Locale.ROOT)
      it.longToast("${it.string(R.string.msg_photo)} ${photo.name} $action")
    }
  }

  private fun setBottomDialog() {
    val activity = activity as AppCompatActivity?
    if (activity != null) {
      val bottomDialog = BottomDialogFragment()
      bottomDialog.setTargetFragment(this, 0)
      bottomDialog.show(activity.supportFragmentManager, BOTTOM)
    }
  }

  override fun onItemClick(dialogInterface: DialogInterface, view: View) {
    val id = view.id
    dialogInterface.cancel()
    val activity = activity ?: return

    when (id) {
      R.id.container_camera -> permissionManager.checkPermissions(
        activity,
        object : PermissionResultCallback {
          override fun onPermissionGranted() {
            createCameraIntent()
          }

          override fun onPermissionDenied(permissionList: Array<String>) {
            val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
              permissionManager.checkPermissions(
                activity,
                this,
                *permissionList
              )
            }
            val action = activity.string(R.string.action_photo).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_camera_rights_description)} $action"
            showPermissionDialogWithCancelButton(activity, message, positiveButton)
          }

          override fun onPermissionPermanentlyDenied(permission: String) {
            val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
              permissionManager.openApplicationSettings(activity)
            }
            val action = activity.string(R.string.action_photo).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_camera_rights_settings_description)} $action"
            showPermissionDialogWithCancelButton(activity, message, positiveButton)
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
            val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
              permissionManager.checkPermissions(
                activity,
                this,
                *permissionList
              )
            }
            val action = activity.string(R.string.action_download).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_memory_rights_description)} $action"
            showPermissionDialogWithCancelButton(activity, message, positiveButton)
          }

          override fun onPermissionPermanentlyDenied(permission: String) {
            val positiveButton = AlertButton(activity.string(R.string.action_ok)) {
              permissionManager.openApplicationSettings(activity)
            }
            val action = activity.string(R.string.action_download).toLowerCase(Locale.ROOT)
            val message = "${activity.string(R.string.text_memory_rights_settings_description)} $action"
            showPermissionDialogWithCancelButton(activity, message, positiveButton)
          }
        },
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
    }
  }

  private fun createCameraIntent() {
    val resultIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    cameraFileUri = FileUtils.getInstance().localFileUri
    resultIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri)
    startActivityForResult(resultIntent, CAMERA_REQUEST)
  }

  private fun createGalleryIntent() {
    val resultIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    resultIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    startActivityForResult(
      Intent.createChooser(resultIntent, getString(R.string.msg_image_chooser)),
      GALLERY_IMAGE_REQUEST
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

  override fun onDestroyView() {
    super.onDestroyView()
    recyclerView?.adapter = null
    recyclerView = null
  }

  override fun layout(): Int {
    return R.layout.fragment_disk
  }

  companion object {

    //Saved state constants
    private const val STATE_FILE_URI = "STATE_FILE_URI"
    private const val STATE_SELECTABLE = "STATE_SELECTABLE"

    //Requests types
    private const val GALLERY_IMAGE_REQUEST = 1999
    private const val CAMERA_REQUEST = 1888

    //Dialog types
    private const val BOTTOM = "BOTTOM"
  }
}