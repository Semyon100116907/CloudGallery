package com.semisonfire.cloudgallery.ui.main.disk.adapter;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.custom.ItemDecorator;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.items.DiskItem;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.items.GalleryItem;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.items.HeaderItem;
import com.semisonfire.cloudgallery.ui.main.disk.adapter.items.UploadItem;
import com.semisonfire.cloudgallery.utils.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DiskAdapter.class.getSimpleName();

    private Map<String, List<Photo>> mMap;
    private List<DiskItem> mDiskItems;
    private SelectableHelper.OnPhotoListener mPhotoClickListener;
    private boolean selected;


    public DiskAdapter(SelectableHelper.OnPhotoListener listener) {
        mPhotoClickListener = listener;
        mMap = new LinkedHashMap<>();
        mDiskItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case DiskItem.TYPE_HEADER: {
                View view = inflater.inflate(R.layout.item_disk_header, parent, false);
                return new HeaderViewHolder(view);
            }
            case DiskItem.TYPE_GALLERY: {
                View view = inflater.inflate(R.layout.item_disk, parent, false);
                return new GalleryViewHolder(view);
            }
            case DiskItem.TYPE_UPLOAD: {
                View view = inflater.inflate(R.layout.item_disk_upload, parent, false);
                return new UploadViewHolder(view);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case DiskItem.TYPE_HEADER: {
                HeaderItem headerItem = (HeaderItem) mDiskItems.get(position);
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.bind(headerItem);
                break;
            }
            case DiskItem.TYPE_GALLERY: {
                GalleryItem galleryItem = (GalleryItem) mDiskItems.get(position);
                GalleryViewHolder galleryViewHolder = (GalleryViewHolder) holder;
                galleryViewHolder.bind(galleryItem);
                break;
            }
            case DiskItem.TYPE_UPLOAD: {
                UploadItem uploadItem = (UploadItem) mDiskItems.get(position);
                UploadViewHolder uploadViewHolder = (UploadViewHolder) holder;
                uploadViewHolder.bind(uploadItem);
                break;
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    public void setPhotos(List<Photo> items) {
        clear();
        Map<String, List<Photo>> map = toMap(items);
        updateItems(map);
    }

    public void addPhotos(List<Photo> items) {
        Map<String, List<Photo>> map = toMap(items);
        updateItems(map);
    }

    /** Clear data set*/
    public void clear() {
        int currentSize = getItemCount();
        mMap.clear();
        mDiskItems.clear();
        notifyItemRangeRemoved(0, currentSize);
    }

    private Map<String, List<Photo>> toMap(List<Photo> photos) {
        Map<String, List<Photo>> map = new LinkedHashMap<>();
        for (Photo photo : photos) {
            String date = DateUtils.getDateString(photo.getModifiedAt(), DateUtils.ONLY_DATE_FORMAT);
            List<Photo> values = map.get(date);
            if (values == null) {
                values = new ArrayList<>();
                map.put(date, values);
            }
            values.add(photo);
        }
        return map;
    }

    /** Update adapter data set */
    private void updateItems(Map<String, List<Photo>> map) {
        for (String date : map.keySet()) {
            List<Photo> values = mMap.get(date);
            HeaderItem headerItem = new HeaderItem();
            headerItem.setDate(date);
            GalleryItem galleryItem;

            if (values != null) {
                int headerPos = mDiskItems.indexOf(headerItem);
                if (headerPos == -1) {
                    headerPos = mDiskItems.get(0) instanceof UploadItem ? 1 : 0;
                    mDiskItems.add(headerPos, headerItem);
                    notifyItemInserted(headerPos);
                }
                int galleryPos = headerPos + 1;
                galleryItem = (GalleryItem) mDiskItems.get(galleryPos);
                galleryItem.getPhotos().addAll(map.get(date));
                mDiskItems.set(galleryPos, galleryItem);

                headerItem.setCount(galleryItem.getPhotos().size());
                mDiskItems.set(headerPos, headerItem);
                notifyItemRangeChanged(headerPos, 2);
            } else {
                values = new ArrayList<>(map.get(date));
                mMap.put(date, values);
                headerItem.setCount(values.size());
                mDiskItems.add(headerItem);

                galleryItem = new GalleryItem();
                galleryItem.setPhotos(values);
                mDiskItems.add(galleryItem);
                notifyItemRangeInserted(getItemCount(), 2);
            }
        }
    }

    /** Add photo inside gallery item sorted by date */
    public void addPhoto(Photo photo) {
        String date = DateUtils.getDateString(photo.getModifiedAt(), DateUtils.ONLY_DATE_FORMAT);

        HeaderItem headerItem = new HeaderItem();
        headerItem.setDate(date);
        List<Photo> values = mMap.get(date);

        int headerPos = mDiskItems.size() > 0 ?
                mDiskItems.indexOf(headerItem) == -1 ? 1 : mDiskItems.indexOf(headerItem) : 0;
        int galleryPos = headerPos + 1;

        if (values == null) {
            values = new ArrayList<>();
            mMap.put(date, values);
            mDiskItems.add(headerPos, headerItem);

            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setPhotos(values);
            mDiskItems.add(galleryPos, galleryItem);
            notifyItemRangeInserted(headerPos, 2);
        }

        GalleryItem galleryItem = (GalleryItem) mDiskItems.get(galleryPos);
        galleryItem.getPhotos().add(0, photo);
        mDiskItems.set(galleryPos, galleryItem);

        headerItem.setCount(values.size());
        mDiskItems.set(headerPos, headerItem);
        notifyItemRangeChanged(headerPos, 2);
    }

    public void setSelection(boolean selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    /** Remove photo from gallery item */
    public void removePhoto(Photo photo) {
        for (int i = 0; i < getItemCount(); i++) {
            DiskItem diskItem = mDiskItems.get(i);
            if (diskItem instanceof GalleryItem) {
                int headerPos = i - 1;
                HeaderItem headerItem = (HeaderItem) mDiskItems.get(headerPos);
                List<Photo> items = ((GalleryItem) diskItem).getPhotos();
                if (items.contains(photo)) {
                    items.remove(photo);
                    if (items.isEmpty()) {
                        mDiskItems.remove(diskItem);
                        mDiskItems.remove(headerPos);
                        notifyItemRemoved(i);
                        return;
                    }
                    notifyItemChanged(i);

                    if (headerItem != null) {
                        headerItem.setCount(items.size());
                        notifyItemChanged(headerPos);
                    }
                    return;
                }
            }
        }
    }

    public void addUploadPhotos(List<Photo> photos) {
        UploadItem uploadItem = new UploadItem();
        if (!mDiskItems.isEmpty() && mDiskItems.get(0) instanceof UploadItem) {
            uploadItem = (UploadItem) mDiskItems.get(0);
            uploadItem.setVisibility(View.VISIBLE);
            uploadItem.addUploadPhotos(photos);
            mDiskItems.set(0, uploadItem);
            notifyItemChanged(0);
        } else {
            uploadItem.addUploadPhotos(photos);
            uploadItem.setVisibility(View.VISIBLE);
            mDiskItems.add(0, uploadItem);
            notifyItemInserted(0);
        }
    }

    public void removeUploadedPhoto(Photo item) {
        UploadItem uploadItem = (UploadItem) mDiskItems.get(0);
        uploadItem.getUploadPhotos().remove(item);
        uploadItem.incrementUpload();
        if (uploadItem.getUploadPhotos().size() == 0) {
            uploadItem.setVisibility(View.GONE);
            uploadItem.resetUploadCount();
            mDiskItems.remove(0);
            notifyItemRemoved(0);
            return;
        }
        notifyItemChanged(0);
    }

    public void changeUploadState(String state) {
        DiskItem diskItem = mDiskItems.get(0);
        if (diskItem instanceof UploadItem) {
            ((UploadItem)diskItem).setState(state);
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDiskItems.get(position) != null ? mDiskItems.get(position).getType() : -1;
    }

    //region ViewHolders
    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mDate;
        private TextView mCount;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mDate = itemView.findViewById(R.id.text_upload_date);
            mCount = itemView.findViewById(R.id.text_photo_count);
        }

        void bind(HeaderItem item) {
            mCount.setText(String.format(Locale.getDefault(), "%d %s", item.getCount(),
                    itemView.getContext().getString(R.string.msg_photo).toLowerCase()));
            mDate.setText(item.getDate());
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView mPhotoRecyclerView;
        private PhotoAdapter mAdapter;
        private ItemDecorator mItemDecorator;

        GalleryViewHolder(View itemView) {
            super(itemView);

            //LayoutManager
            int orientation = itemView.getResources().getConfiguration().orientation;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(itemView.getContext(),
                    orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 3);

            //Item decorator
            mItemDecorator = new ItemDecorator(itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.disk_grid_space));

            //RecyclerView
            mAdapter = new PhotoAdapter(mPhotoClickListener);
            mPhotoRecyclerView = itemView.findViewById(R.id.rv_photos);
            mPhotoRecyclerView.setAdapter(mAdapter);
            mPhotoRecyclerView.setNestedScrollingEnabled(false);
            mPhotoRecyclerView.setFocusable(false);
            mPhotoRecyclerView.setLayoutManager(gridLayoutManager);
            mPhotoRecyclerView.addItemDecoration(mItemDecorator);
        }

        void bind(GalleryItem item) {
            mAdapter.setSelection(selected);
            mAdapter.setPhotos(item.getPhotos());
        }
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        private TextView mState;
        private TextView mTitle;
        private RecyclerView mUploadRecyclerView;
        private LinearLayoutManager mLinearLayoutManager;
        private ItemDecorator mItemDecorator;
        private UploadPhotoAdapter mAdapter;

        public UploadViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.text_upload_title);
            mState = itemView.findViewById(R.id.text_upload_state);
            mUploadRecyclerView = itemView.findViewById(R.id.rv_uploads);
            mAdapter = new UploadPhotoAdapter();
            mLinearLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            mItemDecorator = new ItemDecorator(itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.upload_linear_space));

            mUploadRecyclerView.setAdapter(mAdapter);
            mUploadRecyclerView.setLayoutManager(mLinearLayoutManager);
            mUploadRecyclerView.addItemDecoration(mItemDecorator);
        }

        void bind(UploadItem item) {

            mTitle.setVisibility(item.getVisibility());
            mState.setVisibility(item.getVisibility());
            mUploadRecyclerView.setVisibility(item.getVisibility());

            if (item.getVisibility() == View.VISIBLE) {
                mAdapter.setItems(item.getUploadPhotos());

                String state = TextUtils.isEmpty(item.getState()) ?
                        String.format(Locale.getDefault(), "%d %s %d",
                                item.getUploadCount(),
                                itemView.getContext().getString(R.string.msg_of),
                                item.getSize()) : item.getState();
                mState.setText(state);
            }
        }
    }
    //endregion

    @Override
    public int getItemCount() {
        return mDiskItems.size();
    }
}