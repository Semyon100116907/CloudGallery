package com.semisonfire.cloudgallery.ui.disk.adapter

import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.data.model.Photo
import com.semisonfire.cloudgallery.core.ui.adapter.BaseAdapter
import com.semisonfire.cloudgallery.core.ui.adapter.BaseViewHolder
import com.semisonfire.cloudgallery.core.ui.adapter.ProgressViewHolder
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper.OnPhotoListener
import com.semisonfire.cloudgallery.ui.disk.adapter.items.*
import com.semisonfire.cloudgallery.utils.DateUtils
import com.semisonfire.cloudgallery.utils.DateUtils.getDateString
import com.semisonfire.cloudgallery.utils.dimen
import com.semisonfire.cloudgallery.utils.string
import java.util.*
import kotlin.collections.ArrayList

class DiskAdapter : BaseAdapter<DiskItem, BaseViewHolder<DiskItem>>() {

    private val map = mutableMapOf<String, List<Photo>>()
    private val diskItemList = mutableListOf<DiskItem>()
    private var photoCount = 0

    private var photoListener: OnPhotoListener? = null
    private var selected = false

    fun setPhotoClickListener(listener: OnPhotoListener) {
        photoListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DiskItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_disk_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_GALLERY -> {
                val view = inflater.inflate(R.layout.item_disk, parent, false)
                GalleryViewHolder(view)
            }
            TYPE_UPLOAD -> {
                val view = inflater.inflate(R.layout.item_disk_upload, parent, false)
                UploadViewHolder(view)
            }
            TYPE_PROGRESS -> {
                val view = inflater.inflate(R.layout.item_progress, parent, false)
                ProgressViewHolder(view)
            }
            else -> throw IllegalStateException("unsupported item type")
        } as BaseViewHolder<DiskItem>
    }

    override fun onBindViewHolder(
        viewHolder: BaseViewHolder<DiskItem>,
        position: Int
    ) {
        val diskItem = diskItemList[position]
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                val headerItem = diskItem as HeaderItem
                val headerViewHolder = viewHolder as HeaderViewHolder
                headerViewHolder.bindItem(headerItem)
            }
            TYPE_GALLERY -> {
                val galleryItem = diskItem as GalleryItem
                val galleryViewHolder = viewHolder as GalleryViewHolder
                galleryViewHolder.bindItem(galleryItem)
            }
            TYPE_UPLOAD -> {
                val uploadItem = diskItem as UploadItem
                val uploadViewHolder = viewHolder as UploadViewHolder
                uploadViewHolder.bindItem(uploadItem)
            }
            else -> throw IllegalStateException("unsupported item type")
        }

        onLoadMore(position)
    }

    override fun checkNextThreshold(position: Int): Boolean {
        val threshold = photoCount - endlessScrollThreshold
        val progressPosition = progressItem?.let { items.indexOf(it) } ?: -1
        return position == progressPosition || position < threshold
    }

    fun setPhotos(items: List<Photo>) {
        clear()
        updateItems(toMap(items))

        photoCount = items.size
    }

    fun addPhotos(items: List<Photo>) {
        updateItems(toMap(items))
        onLoadMoreComplete(emptyList())

        photoCount += items.size
    }

    /**
     * Clear data set
     */
    fun clear() {
        val currentSize = itemCount
        map.clear()
        diskItemList.clear()
        notifyItemRangeRemoved(0, currentSize)
    }

    /**
     * Transform [List]<[Photo]> into map which will used inside [.updateItems]
     */
    private fun toMap(photos: List<Photo>): Map<String, List<Photo>> {
        val map = mutableMapOf<String, List<Photo>>()
        for (photo in photos) {
            val date = getDateString(photo.modifiedAt, DateUtils.DATE_FORMAT)
            var values = map[date] as MutableList<Photo>?

            if (values == null) {
                values = mutableListOf()
                if (date != null) {
                    map[date] = values
                }
            }
            values.add(photo)
        }
        return map
    }

    /**
     * Update adapter data set
     */
    private fun updateItems(map: Map<String, List<Photo>>) {
        val keys = map.keys.toList()
        for (i in keys.size - 1 downTo 0) {
            val date = keys[i]

            val headerItem = HeaderItem()
            headerItem.date = date

            val photoList = map[date] ?: continue
            var values = this.map[date]
            if (values != null) {
                val headerPos = insertHeaderItem(diskItemList.indexOf(headerItem), headerItem)
                val galleryPos = headerPos + 1

                val galleryItem = diskItemList[galleryPos]
                if (galleryItem !is GalleryItem) continue

                galleryItem.getMutablePhotos().addAll(photoList)
                diskItemList[galleryPos] = galleryItem
                headerItem.count = galleryItem.photos.size
                diskItemList[headerPos] = headerItem
                notifyItemRangeChanged(headerPos, 2)
            } else {
                values = ArrayList(photoList)
                this.map[date] = values
                headerItem.count = values.size
                diskItemList.add(headerItem)

                val galleryItem = GalleryItem()
                galleryItem.photos = values
                diskItemList.add(galleryItem)
                notifyItemRangeInserted(itemCount, 2)
            }
        }
    }

    private fun insertHeaderItem(
        headerPos: Int,
        headerItem: HeaderItem
    ): Int {
        if (headerPos == -1) {
            val insertPosition = if (diskItemList.getOrNull(0) is UploadItem) 1 else 0
            diskItemList.add(insertPosition, headerItem)
            notifyItemInserted(insertPosition)
            return insertPosition
        }
        return headerPos
    }

    /**
     * Add photo inside gallery item sorted by date
     */
    fun addPhoto(photo: Photo) {

        val date = getDateString(photo.modifiedAt, DateUtils.DATE_FORMAT) ?: ""
        val headerItem = HeaderItem()
        headerItem.date = date

        val headerPos = if (diskItemList.size > 0) {
            if (diskItemList.indexOf(headerItem) == -1) 1 else diskItemList.indexOf(headerItem)
        } else {
            0
        }
        val galleryPos = headerPos + 1

        var values = map[date]
        if (values == null) {
            values = ArrayList()
            map[date] = values
            diskItemList.add(headerPos, headerItem)
            val galleryItem = GalleryItem()
            galleryItem.photos = values
            diskItemList.add(galleryPos, galleryItem)
            notifyItemRangeInserted(headerPos, 2)
        }

        val galleryItem = diskItemList[galleryPos]
        if (galleryItem is GalleryItem) {
            galleryItem.getMutablePhotos().add(0, photo)
            diskItemList[galleryPos] = galleryItem
            headerItem.count = values.size
            diskItemList[headerPos] = headerItem
            notifyItemRangeChanged(headerPos, 2)
            photoCount++
        }
    }

    fun setSelection(selected: Boolean) {
        this.selected = selected
        notifyDataSetChanged()
    }

    /**
     * Remove photo from gallery item
     */
    fun removePhoto(photo: Photo?) {
        for (i in 0 until itemCount) {
            val diskItem = diskItemList[i]
            if (diskItem !is GalleryItem) continue

            val headerPos = i - 1
            val headerItem = diskItemList[headerPos] as HeaderItem?

            val items = diskItem.getMutablePhotos()
            if (!items.contains(photo)) continue

            items.remove(photo)
            photoCount--

            if (items.isEmpty()) {
                diskItemList.remove(diskItem)
                diskItemList.removeAt(headerPos)
                notifyItemRemoved(i)
                return
            }
            notifyItemChanged(i)

            if (headerItem != null) {
                headerItem.count = items.size
                notifyItemChanged(headerPos)
            }
            return
        }
    }

    fun addUploadPhotos(photos: List<Photo>) {
        val uploadItem: DiskItem?
        if (diskItemList.isNotEmpty() && diskItemList[0] is UploadItem) {
            uploadItem = diskItemList[0]
            if (uploadItem !is UploadItem) return

            uploadItem.visibility = View.VISIBLE
            uploadItem.addUploadPhotos(photos)
            diskItemList[0] = uploadItem
            notifyItemChanged(0)
        } else {
            uploadItem = UploadItem()
            uploadItem.addUploadPhotos(photos)
            uploadItem.visibility = View.VISIBLE
            diskItemList.add(0, uploadItem)
            notifyItemInserted(0)
        }
    }

    fun removeUploadedPhoto(item: Photo?) {
        val uploadItem = diskItemList[0]
        if (uploadItem !is UploadItem) return

        val uploadPhotos = uploadItem.getUploadPhotos()
        uploadPhotos.remove(item)
        uploadItem.incrementUpload()

        if (uploadPhotos.isEmpty()) {
            uploadItem.visibility = View.GONE
            uploadItem.resetUploadCount()
            diskItemList.removeAt(0)
            notifyItemRemoved(0)
            return
        }
        notifyItemChanged(0)
    }

    fun changeUploadState(state: String?) {
        val diskItem = diskItemList[0]
        if (diskItem !is UploadItem) return

        diskItem.state = state
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        val item = diskItemList.getOrNull(position)
        return item?.type ?: -1
    }

    override fun getItemCount(): Int {
        return diskItemList.size
    }

    internal inner class HeaderViewHolder(itemView: View) :
        BaseViewHolder<HeaderItem>(itemView) {

        private val dateTextView = itemView.findViewById<TextView>(R.id.text_upload_date)
        private val countTextView = itemView.findViewById<TextView>(R.id.text_photo_count)

        override fun bindItem(item: HeaderItem) {
            val text = "${item.count} ${
                itemView.context.string(R.string.msg_photo).toLowerCase(Locale.ROOT)
            }"
            countTextView.text = text
            dateTextView.text = item.date
        }
    }

    internal inner class GalleryViewHolder(itemView: View) :
        BaseViewHolder<GalleryItem>(itemView) {

        private val adapter = PhotoAdapter()

        override fun bindItem(item: GalleryItem) {
            adapter.setSelection(selected)
            adapter.updateDataSet(item.photos)
        }

        init {
            //LayoutManager
            val orientation = itemView.resources.configuration.orientation
            val gridLayoutManager = GridLayoutManager(
                itemView.context,
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 3
            )
            //Item decorator
            val itemDecorator = ItemDecorator(itemView.context.dimen(R.dimen.disk_grid_space))

            //RecyclerView
            photoListener?.let {
                adapter.setPhotoListener(it)
            }

            val recyclerView = itemView.findViewById<RecyclerView>(R.id.rv_photos)
            recyclerView.adapter = adapter
            recyclerView.isNestedScrollingEnabled = false
            recyclerView.isFocusable = false
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.addItemDecoration(itemDecorator)
        }
    }

    internal inner class UploadViewHolder(itemView: View) :
        BaseViewHolder<UploadItem>(itemView) {

        private val stateTextView = itemView.findViewById<TextView>(R.id.text_upload_state)
        private val titleTextView = itemView.findViewById<TextView>(R.id.text_upload_title)
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.rv_uploads)

        private val adapter = UploadPhotoAdapter()

        override fun bindItem(item: UploadItem) {

            val visibility = item.visibility
            titleTextView.visibility = visibility
            stateTextView.visibility = visibility
            recyclerView.visibility = visibility

            if (visibility == View.VISIBLE) {
                adapter.updateDataSet(item.getUploadPhotos())
                val itemState = item.state
                val state = if (itemState.isNullOrEmpty()) {
                    "${item.uploadCount} ${itemView.context.string(R.string.msg_of)} ${item.size}"
                } else {
                    itemState
                }
                stateTextView.text = state
            }
        }

        init {
            val layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            val itemDecorator = ItemDecorator(itemView.context.dimen(R.dimen.upload_linear_space))
            recyclerView.adapter = adapter
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(itemDecorator)
        }
    }
}