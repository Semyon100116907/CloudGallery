package com.semisonfire.cloudgallery.ui.trash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.R.dimen
import com.semisonfire.cloudgallery.adapter.holder.Item
import com.semisonfire.cloudgallery.core.ui.ContentFragment
import com.semisonfire.cloudgallery.databinding.FragmentTrashBinding
import com.semisonfire.cloudgallery.di.provider.provideComponent
import com.semisonfire.cloudgallery.ui.detail.PhotoDetailActivity
import com.semisonfire.cloudgallery.ui.trash.adapter.TrashBinAdapter
import com.semisonfire.cloudgallery.ui.trash.di.DaggerTrashBinComponent
import com.semisonfire.cloudgallery.ui.trash.model.TrashBinResult
import com.semisonfire.cloudgallery.utils.dimen
import com.semisonfire.cloudgallery.utils.foreground
import javax.inject.Inject

class TrashFragment : ContentFragment() {

    @Inject
    lateinit var presenter: TrashPresenter

    @Inject
    lateinit var adapter: TrashBinAdapter

    private var _viewBinding: FragmentTrashBinding? = null

    private val viewBinding: FragmentTrashBinding
        get() = _viewBinding!!

    private var floatingActionButton: FloatingActionButton? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var openDetail: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        openDetail = registerForActivityResult(StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                val isDataChanged =
                    result.data?.getBooleanExtra(PhotoDetailActivity.EXTRA_CHANGED, false) ?: false
                if (isDataChanged) {
                    presenter.getPhotos(0)
                }
            }
        }

        DaggerTrashBinComponent
            .factory()
            .create(
                context.provideComponent()
            )
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.getPhotos(0)
    }

    override fun layout(): Int {
        return R.layout.fragment_trash
    }

    public override fun bind(view: View) {
        super.bind(view)
        updateToolbarTitle(getString(R.string.msg_trash))

        _viewBinding = FragmentTrashBinding.bind(view)

        activity?.let {
            floatingActionButton = it.findViewById(R.id.btn_add_new)
            swipeRefreshLayout = it.findViewById(R.id.swipe_refresh)
        }
        floatingActionButton?.hide()

        viewBinding.rvTrash.adapter = adapter

        val orientation = resources.configuration.orientation
        viewBinding.rvTrash.layoutManager = GridLayoutManager(
            context,
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 3
        )

        val itemDecorator = TrashBinItemDecorator(view.context.dimen(dimen.disk_grid_space))
        viewBinding.rvTrash.addItemDecoration(itemDecorator)
        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = true
            presenter.getPhotos(0)
        }
    }

    override fun onStart() {
        super.onStart()
        disposables.add(
            presenter
                .observeTrashBinResult()
                .observeOn(foreground())
                .subscribe {
                    when (it) {
                        is TrashBinResult.Loaded -> onTrashLoaded(it.photos)
                        TrashBinResult.Cleared -> onTrashCleared()
                    }
                }
        )
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dispose()
    }

    private fun onTrashLoaded(photos: List<Item>) {
        swipeRefreshLayout?.isRefreshing = false
        adapter.updateDataSet(photos)
    }

    private fun onTrashCleared() {
        adapter.updateDataSet(emptyList())
    }
}