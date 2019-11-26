package com.semisonfire.cloudgallery.ui.trash

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.core.ui.BaseFragment
import com.semisonfire.cloudgallery.data.model.Photo
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper.OnPhotoListener
import com.semisonfire.cloudgallery.ui.dialogs.AlertDialogFragment
import com.semisonfire.cloudgallery.ui.dialogs.base.DialogListener
import com.semisonfire.cloudgallery.ui.disk.adapter.PhotoAdapter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.utils.color
import com.semisonfire.cloudgallery.utils.colorResDrawable
import com.semisonfire.cloudgallery.utils.longToast
import com.semisonfire.cloudgallery.utils.string
import java.util.*

interface TrashView : MvpView {

  fun onTrashLoaded(photos: List<Photo>)
  fun onPhotoDeleted(photo: Photo)
  fun onPhotoRestored(photo: Photo)
  fun onTrashCleared()
}

class TrashFragment :
  BaseFragment<TrashView, TrashPresenter>(),
  TrashView, DialogListener {

  //RecyclerView
  private var recyclerView: RecyclerView? = null
  private val photoAdapter = PhotoAdapter()

  private val trashPhotoList: MutableList<Photo> = mutableListOf()
  private val selectedPhotos: MutableList<Photo> = ArrayList()

  private var floatingActionButton: FloatingActionButton? = null
  private var swipeRefreshLayout: SwipeRefreshLayout? = null
  private var isSelectable = false

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (savedInstanceState != null) {
      isSelectable = savedInstanceState.getBoolean(STATE_SELECTABLE)
      setSelectableItems()
    }
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  private fun setSelectableItems() {
    for (photo in trashPhotoList) {
      if (photo.isSelected) {
        selectedPhotos.add(photo)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(STATE_SELECTABLE, isSelectable)
  }

  public override fun bind(view: View) {
    super.bind(view)
    val activity = activity
    if (activity != null) {
      floatingActionButton = activity.findViewById(R.id.btn_add_new)
      swipeRefreshLayout = activity.findViewById(R.id.swipe_refresh)
    }
    // setScrollView(recyclerView);
    floatingActionButton?.hide()

    photoAdapter.setPhotoListener(object : OnPhotoListener {
      override fun onPhotoClick(photos: List<Photo>, position: Int) {
        if (context != null) {
          val intent = Intent(context, PhotoDetailActivity::class.java)
          intent.putExtra(PhotoDetailActivity.EXTRA_CURRENT_PHOTO, position)
          intent.putParcelableArrayListExtra(
            PhotoDetailActivity.EXTRA_PHOTOS,
            trashPhotoList as ArrayList<out Parcelable>
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
    photoAdapter.updateDataSet(trashPhotoList)
    //RecyclerView

    val recyclerView = view.findViewById<RecyclerView>(R.id.rv_trash)
    recyclerView.adapter = photoAdapter

    val orientation = resources.configuration.orientation
    val gridLayoutManager = GridLayoutManager(
      context,
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 3
    )
    recyclerView.layoutManager = gridLayoutManager

    val itemDecorator = ItemDecorator(resources.getDimensionPixelOffset(R.dimen.disk_grid_space))
    recyclerView.addItemDecoration(itemDecorator)
    swipeRefreshLayout?.setOnRefreshListener {
      swipeRefreshLayout?.isRefreshing = true
      updateDataSet()
    }
  }

  private fun updateDataSet() {
    trashPhotoList.clear()
    photoAdapter.updateDataSet(trashPhotoList)
    //        mTrashPresenter.getPhotos(mCurrentPage);
  }

  private fun showEmpty() {
//        getStateView().showEmptyView(
//                R.drawable.ic_delete,
//                getString(R.string.msg_yandex_trash_empty),
//                null
//        );
  }

  override fun onCreateOptionsMenu(
    menu: Menu,
    inflater: MenuInflater
  ) {
    super.onCreateOptionsMenu(menu, inflater)
    menu.findItem(R.id.menu_restore_all).isVisible = true
    menu.findItem(R.id.menu_delete_all).isVisible = true
    setEnabledSelection(isSelectable)
    photoAdapter.setSelection(isSelectable)
    updateToolbarTitle(selectedPhotos.size.toString())
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> setEnabledSelection(false)
      R.id.menu_restore_all -> if (isSelectable && selectedPhotos.size != trashPhotoList.size) {
        presenter.restorePhotos(selectedPhotos)
      } else {
        presenter.restorePhotos(trashPhotoList)
      }
      R.id.menu_delete_all -> if (isSelectable && selectedPhotos.size != trashPhotoList.size) {
        presenter.deletePhotos(selectedPhotos)
      } else {
        if (trashPhotoList.isNotEmpty() || selectedPhotos.isNotEmpty()) {
          showDialog(
            getString(R.string.msg_clear_trash),
            getString(R.string.msg_clear_trash_description),
            context?.color(R.color.colorAccent) ?: Color.BLUE
          )
        }
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun setEnabledSelection(enabled: Boolean) {
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

    swipeRefreshLayout?.isEnabled = !enabled

    isSelectable = enabled
    photoAdapter.setSelection(enabled)
    if (!enabled) {
      selectedPhotos.clear()
      updateToolbarTitle(getString(R.string.msg_trash))
    }
  }

  fun onInternetUnavailable() {
    if (trashPhotoList.isEmpty()) {
//      getStateView().showEmptyView(
//        R.drawable.ic_delete,
//        getString(R.string.msg_yandex_failed_retrieve),
//        getString(R.string.action_yandex_check_connection)
//      )
    }
  }

  override fun onTrashLoaded(photos: List<Photo>) {
    swipeRefreshLayout?.isRefreshing = false
    if (photos.isNotEmpty()) {
      trashPhotoList.addAll(photos)
      photoAdapter.addItems(photos)
//      getStateView().hideStateView()
    } else {
      if (trashPhotoList.isEmpty()) {
        showEmpty()
      }
    }
  }

  override fun onPhotoRestored(photo: Photo) {
    trashPhotoList.remove(photo)
    photoAdapter.removeItem(photo)

    context?.let {
      val action = it.string(R.string.msg_restored).toLowerCase(Locale.ROOT)
      it.longToast("${it.string(R.string.msg_photo)} ${photo.name} $action")
    }
    setEnabledSelection(false)
    if (trashPhotoList.isEmpty()) {
      showEmpty()
    }
  }

  override fun onPhotoDeleted(photo: Photo) {
    trashPhotoList.remove(photo)
    photoAdapter.removeItem(photo)

    context?.let {
      val action = it.string(R.string.msg_deleted).toLowerCase(Locale.ROOT)
      it.longToast("${it.string(R.string.msg_photo)} ${photo.name} $action")
    }
    setEnabledSelection(false)
    if (trashPhotoList.isEmpty()) {
      showEmpty()
    }
  }

  override fun onTrashCleared() {
    trashPhotoList.clear()
    photoAdapter.updateDataSet(ArrayList())
    setEnabledSelection(false)
    showEmpty()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK && requestCode == PhotoDetailActivity.DETAIL_REQUEST) {
      val isDataChanged = data.getBooleanExtra(
        PhotoDetailActivity.EXTRA_CHANGED,
        false
      )
      if (isDataChanged) {
        updateDataSet()
      }
    }
  }

  private fun showDialog(title: String, message: String, color: Int) {
    val activity = activity as AppCompatActivity?
    if (activity != null) {
      val mAlertDialog = AlertDialogFragment.newInstance(title, message, color)
      mAlertDialog.setTargetFragment(this, 0)
      mAlertDialog.show(activity.supportFragmentManager, "alert")
    }
  }

  override fun onPositiveClick(dialogInterface: DialogInterface) {
    presenter.clear()
    dialogInterface.cancel()
  }

  override fun onNegativeClick(dialogInterface: DialogInterface) {
    dialogInterface.cancel()
  }

  override fun onItemClick(
    dialogInterface: DialogInterface,
    view: View
  ) {
  }

  override fun onDestroyView() {
    super.onDestroyView()
    recyclerView?.adapter = null
    recyclerView = null
  }

  override fun layout(): Int {
    return R.layout.fragment_trash
  }

  companion object {
    //State
    private const val STATE_SELECTABLE = "STATE_SELECTABLE"
  }
}