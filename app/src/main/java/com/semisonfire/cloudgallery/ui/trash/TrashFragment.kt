package com.semisonfire.cloudgallery.ui.trash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.mvp.MvpView
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper.OnPhotoListener
import com.semisonfire.cloudgallery.ui.di.provideComponent
import com.semisonfire.cloudgallery.ui.dialogs.AlertDialogFragment
import com.semisonfire.cloudgallery.ui.dialogs.DialogListener
import com.semisonfire.cloudgallery.ui.disk.adapter.PhotoAdapter
import com.semisonfire.cloudgallery.ui.photo.PhotoDetailActivity
import com.semisonfire.cloudgallery.ui.selectable.SelectableFragment
import com.semisonfire.cloudgallery.ui.trash.di.DaggerTrashBinComponent
import com.semisonfire.cloudgallery.ui.trash.model.TrashViewModel
import com.semisonfire.cloudgallery.utils.color
import com.semisonfire.cloudgallery.utils.longToast
import com.semisonfire.cloudgallery.utils.string
import java.util.*

interface TrashView : MvpView<TrashViewModel> {

    fun onTrashLoaded(photos: List<Photo>)
    fun onPhotoDeleted(photo: Photo)
    fun onPhotoRestored(photo: Photo)
    fun onTrashCleared()
}

class TrashFragment : SelectableFragment<TrashViewModel, TrashView, TrashPresenter>(), TrashView {

    private val photoAdapter = PhotoAdapter()

    private val trashPhotoList: MutableList<Photo> = mutableListOf()
    private val selectedPhotos: MutableList<Photo> = ArrayList()

    private var floatingActionButton: FloatingActionButton? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onAttach(context: Context) {
        DaggerTrashBinComponent
            .factory()
            .create(
                context.provideComponent(),
                context.provideComponent()
            )
            .inject(this)
        super.onAttach(context)
    }

    override fun layout(): Int {
        return R.layout.fragment_trash
    }

    override fun menuRes(): Int {
        return R.menu.menu_fragment
    }

    public override fun bind(view: View) {
        super.bind(view)

        activity?.let {
            floatingActionButton = it.findViewById(R.id.btn_add_new)
            swipeRefreshLayout = it.findViewById(R.id.swipe_refresh)
        }
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_trash)
        recyclerView.adapter = photoAdapter

        val orientation = resources.configuration.orientation
        val gridLayoutManager = GridLayoutManager(
            context,
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 3
        )
        recyclerView.layoutManager = gridLayoutManager

        val itemDecorator =
            ItemDecorator(resources.getDimensionPixelOffset(R.dimen.disk_grid_space))
        recyclerView.addItemDecoration(itemDecorator)
        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = true
            updateDataSet()
        }
    }

    private fun updateDataSet() {
        trashPhotoList.clear()
        photoAdapter.updateDataSet(trashPhotoList)
        presenter.getPhotos(0)
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

        menu.apply {
            findItem(R.id.menu_restore_all).isVisible = true
            findItem(R.id.menu_delete_all).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> setEnabledSelection(false)
            R.id.menu_restore_all -> {
                if (isSelectable && selectedPhotos.size != trashPhotoList.size) {
                    presenter.restorePhotos(selectedPhotos)
                } else {
                    presenter.restorePhotos(trashPhotoList)
                }
            }
            R.id.menu_delete_all -> {
                if (isSelectable && selectedPhotos.size != trashPhotoList.size) {
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setEnabledSelection(enabled: Boolean) {
        super.setEnabledSelection(enabled)

        swipeRefreshLayout?.isEnabled = !enabled

        photoAdapter.setSelection(enabled)
        if (!enabled) {
            selectedPhotos.clear()
            updateToolbarTitle(getString(R.string.msg_trash))
        } else {
            updateToolbarTitle(selectedPhotos.size.toString())
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
            val action = it.string(R.string.msg_restored).lowercase()
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
            val action = it.string(R.string.msg_deleted).lowercase()
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
        data: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoDetailActivity.DETAIL_REQUEST) {
            val isDataChanged =
                data?.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false) ?: false
            if (isDataChanged) {
                updateDataSet()
            }
        }
    }

    private fun showDialog(title: String, message: String, color: Int) {
        val activity = activity as AppCompatActivity?
        if (activity != null) {
            val alertDialog = AlertDialogFragment.newInstance(title, message, color)
            alertDialog.dialogListener = object : DialogListener() {
                override fun onPositiveClick() {
                    presenter.clear()
                }
            }
            alertDialog.show(activity.supportFragmentManager, "alert")
        }
    }
}