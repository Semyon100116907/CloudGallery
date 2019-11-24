package com.semisonfire.cloudgallery.ui.disk.adapter;

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
import com.semisonfire.cloudgallery.ui.disk.adapter.items.DiskItem;
import com.semisonfire.cloudgallery.ui.disk.adapter.items.GalleryItem;
import com.semisonfire.cloudgallery.ui.disk.adapter.items.HeaderItem;
import com.semisonfire.cloudgallery.ui.disk.adapter.items.UploadItem;
import com.semisonfire.cloudgallery.utils.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Map<String, List<Photo>> map = new LinkedHashMap<>();
    private List<DiskItem> diskItemList = new ArrayList<>();

    private SelectableHelper.OnPhotoListener photoListener;
    private boolean selected;

    public DiskAdapter() {
    }

    public void setPhotoClickListener(SelectableHelper.OnPhotoListener listener) {
        photoListener = listener;
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
                HeaderItem headerItem = (HeaderItem) diskItemList.get(position);
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.bind(headerItem);
                break;
            }
            case DiskItem.TYPE_GALLERY: {
                GalleryItem galleryItem = (GalleryItem) diskItemList.get(position);
                GalleryViewHolder galleryViewHolder = (GalleryViewHolder) holder;
                galleryViewHolder.bind(galleryItem);
                break;
            }
            case DiskItem.TYPE_UPLOAD: {
                UploadItem uploadItem = (UploadItem) diskItemList.get(position);
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

    /**
     * Clear data set
     */
    public void clear() {
        int currentSize = getItemCount();
        map.clear();
        diskItemList.clear();
        notifyItemRangeRemoved(0, currentSize);
    }

    /**
     * Transform {@link List}<{@link Photo}> into map which will used inside {@link #updateItems(Map)}
     */
    private Map<String, List<Photo>> toMap(List<Photo> photos) {
        Map<String, List<Photo>> map = new LinkedHashMap<>();
        for (Photo photo : photos) {
            String date =
                    DateUtils.getDateString(photo.getModifiedAt(), DateUtils.ONLY_DATE_FORMAT);
            List<Photo> values = map.get(date);
            if (values == null) {
                values = new ArrayList<>();
                if (date != null) {
                    map.put(date, values);
                }
            }
            values.add(photo);
        }
        return map;
    }

    /**
     * Update adapter data set
     */
    private void updateItems(Map<String, List<Photo>> map) {
        for (String date : map.keySet()) {
            List<Photo> values = this.map.get(date);
            HeaderItem headerItem = new HeaderItem();
            headerItem.setDate(date);
            GalleryItem galleryItem;

            final List<Photo> photoList = map.get(date);
            if (photoList == null) continue;

            if (values != null) {
                int headerPos = diskItemList.indexOf(headerItem);
                if (headerPos == -1) {
                    headerPos = diskItemList.get(0) instanceof UploadItem ? 1 : 0;
                    diskItemList.add(headerPos, headerItem);
                    notifyItemInserted(headerPos);
                }
                int galleryPos = headerPos + 1;
                galleryItem = (GalleryItem) diskItemList.get(galleryPos);
                galleryItem.getPhotos().addAll(photoList);
                diskItemList.set(galleryPos, galleryItem);

                headerItem.setCount(galleryItem.getPhotos().size());
                diskItemList.set(headerPos, headerItem);
                notifyItemRangeChanged(headerPos, 2);
            } else {
                values = new ArrayList<>(photoList);
                this.map.put(date, values);
                headerItem.setCount(values.size());
                diskItemList.add(headerItem);

                galleryItem = new GalleryItem();
                galleryItem.setPhotos(values);
                diskItemList.add(galleryItem);
                notifyItemRangeInserted(getItemCount(), 2);
            }
        }
    }

    /**
     * Add photo inside gallery item sorted by date
     */
    public void addPhoto(Photo photo) {
        String date = DateUtils.getDateString(photo.getModifiedAt(), DateUtils.ONLY_DATE_FORMAT);

        HeaderItem headerItem = new HeaderItem();
        headerItem.setDate(date);
        List<Photo> values = map.get(date);

        int headerPos = diskItemList.size() > 0 ?
                diskItemList.indexOf(headerItem) == -1 ? 1 : diskItemList.indexOf(headerItem) : 0;
        int galleryPos = headerPos + 1;

        if (values == null) {
            values = new ArrayList<>();
            map.put(date, values);
            diskItemList.add(headerPos, headerItem);

            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setPhotos(values);
            diskItemList.add(galleryPos, galleryItem);
            notifyItemRangeInserted(headerPos, 2);
        }

        GalleryItem galleryItem = (GalleryItem) diskItemList.get(galleryPos);
        galleryItem.getPhotos().add(0, photo);
        diskItemList.set(galleryPos, galleryItem);

        headerItem.setCount(values.size());
        diskItemList.set(headerPos, headerItem);
        notifyItemRangeChanged(headerPos, 2);
    }

    public void setSelection(boolean selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    /**
     * Remove photo from gallery item
     */
    public void removePhoto(Photo photo) {
        for (int i = 0; i < getItemCount(); i++) {
            DiskItem diskItem = diskItemList.get(i);
            if (diskItem instanceof GalleryItem) {
                int headerPos = i - 1;
                HeaderItem headerItem = (HeaderItem) diskItemList.get(headerPos);
                List<Photo> items = ((GalleryItem) diskItem).getPhotos();
                if (items.contains(photo)) {
                    items.remove(photo);
                    if (items.isEmpty()) {
                        diskItemList.remove(diskItem);
                        diskItemList.remove(headerPos);
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
        if (!diskItemList.isEmpty() && diskItemList.get(0) instanceof UploadItem) {
            uploadItem = (UploadItem) diskItemList.get(0);
            uploadItem.setVisibility(View.VISIBLE);
            uploadItem.addUploadPhotos(photos);
            diskItemList.set(0, uploadItem);
            notifyItemChanged(0);
        } else {
            uploadItem.addUploadPhotos(photos);
            uploadItem.setVisibility(View.VISIBLE);
            diskItemList.add(0, uploadItem);
            notifyItemInserted(0);
        }
    }

    public void removeUploadedPhoto(Photo item) {
        UploadItem uploadItem = (UploadItem) diskItemList.get(0);
        uploadItem.getUploadPhotos().remove(item);
        uploadItem.incrementUpload();
        if (uploadItem.getUploadPhotos().size() == 0) {
            uploadItem.setVisibility(View.GONE);
            uploadItem.resetUploadCount();
            diskItemList.remove(0);
            notifyItemRemoved(0);
            return;
        }
        notifyItemChanged(0);
    }

    public void changeUploadState(String state) {
        DiskItem diskItem = diskItemList.get(0);
        if (diskItem instanceof UploadItem) {
            ((UploadItem) diskItem).setState(state);
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return diskItemList.get(position) != null ? diskItemList.get(position).getType() : -1;
    }

    //region ViewHolders
    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTextView;
        private TextView countTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.text_upload_date);
            countTextView = itemView.findViewById(R.id.text_photo_count);
        }

        void bind(HeaderItem item) {
            final String text = String.format(
                    Locale.getDefault(), "%d %s",
                    item.getCount(),
                    itemView.getContext().getString(R.string.msg_photo).toLowerCase()
            );
            countTextView.setText(text);
            dateTextView.setText(item.getDate());
        }
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {

        private PhotoAdapter adapter = new PhotoAdapter();

        GalleryViewHolder(View itemView) {
            super(itemView);

            //LayoutManager
            int orientation = itemView.getResources().getConfiguration().orientation;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(
                    itemView.getContext(),
                    orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 3
            );

            //Item decorator
            ItemDecorator itemDecorator = new ItemDecorator(itemView.getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.disk_grid_space));

            //RecyclerView
            adapter.setPhotoListener(photoListener);
            RecyclerView recyclerView = itemView.findViewById(R.id.rv_photos);
            recyclerView.setAdapter(adapter);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setFocusable(false);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(itemDecorator);
        }

        void bind(GalleryItem item) {
            adapter.setSelection(selected);
            adapter.setPhotos(item.getPhotos());
        }
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        private TextView stateTextView;
        private TextView titleTextView;
        private RecyclerView recyclerView;
        private UploadPhotoAdapter adapter = new UploadPhotoAdapter();

        public UploadViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_upload_title);
            stateTextView = itemView.findViewById(R.id.text_upload_state);
            recyclerView = itemView.findViewById(R.id.rv_uploads);

            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            );
            ItemDecorator itemDecorator = new ItemDecorator(itemView.getContext().getResources()
                    .getDimensionPixelOffset(R.dimen.upload_linear_space));
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(itemDecorator);
        }

        void bind(UploadItem item) {

            titleTextView.setVisibility(item.getVisibility());
            stateTextView.setVisibility(item.getVisibility());
            recyclerView.setVisibility(item.getVisibility());

            if (item.getVisibility() == View.VISIBLE) {
                adapter.setItems(item.getUploadPhotos());

                String state = TextUtils.isEmpty(item.getState()) ?
                        String.format(Locale.getDefault(), "%d %s %d",
                                item.getUploadCount(),
                                itemView.getContext().getString(R.string.msg_of),
                                item.getSize()
                        ) : item.getState();
                stateTextView.setText(state);
            }
        }
    }
    //endregion

    @Override
    public int getItemCount() {
        return diskItemList.size();
    }
}