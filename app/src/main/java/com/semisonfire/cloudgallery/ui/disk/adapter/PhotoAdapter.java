package com.semisonfire.cloudgallery.ui.disk.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.custom.PhotoDiffUtil;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private SelectableHelper.OnPhotoListener photoListener;
    private List<Photo> photos = new ArrayList<>();

    public PhotoAdapter() {
    }

    public void setPhotoListener(SelectableHelper.OnPhotoListener photoListener) {
        this.photoListener = photoListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setPhotos(List<Photo> items) {
        final PhotoDiffUtil diffUtilCallback = new PhotoDiffUtil(items, photos);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback);
        photos.clear();
        photos.addAll(items);
        diffResult.dispatchUpdatesTo(this);
    }

    public void addPhotos(List<? extends Photo> photos) {
        this.photos.addAll(photos);
        notifyItemRangeInserted(getItemCount(), photos.size());
    }

    public void setSelection(boolean selected) {
        if (!selected) {
            for (Photo photo : photos) {
                if (photo.isSelected()) {
                    photo.setSelected(false);
                }
            }
        }
        SelectableHelper.setMultipleSelection(selected);
        notifyDataSetChanged();
    }

    public void remove(Photo photo) {
        int position = photos.indexOf(photo);
        photos.remove(photo);
        notifyItemRemoved(position);
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private int targetHeight;
        private int targetWidth;
        private ImageView photoImage;
        private ImageView selectImage;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.image_photo);
            selectImage = itemView.findViewById(R.id.image_selected);
            targetWidth = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_max_width);
            targetHeight = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_max_height);
        }

        void bind(Photo photo) {

            View.OnClickListener onItemClick = v -> {
                if (photoListener != null) {
                    Photo p = photos.get(getAdapterPosition());
                    if (!SelectableHelper.getMultipleSelection()) {
                        photoListener.onPhotoClick(photos, getAdapterPosition());
                    } else {
                        p.setSelected(!p.isSelected());
                        notifyItemChanged(getAdapterPosition());
                        photoListener.onSelectedPhotoClick(p);
                    }
                }
            };

            photoImage.setImageDrawable(null);
            Picasso.get().load(photo.getPreview())
                    .resize(targetWidth, targetHeight)
                    .centerCrop()
                    .placeholder(R.color.black)
                    .error(R.drawable.ic_gallery)
                    .into(photoImage);

            photoImage.setOnClickListener(onItemClick);
            selectImage.setOnClickListener(onItemClick);
            selectImage.setVisibility(photo.isSelected() ? View.VISIBLE : View.GONE);

            photoImage.setOnLongClickListener(v -> {
                if (photoListener != null && !SelectableHelper.getMultipleSelection()) {
                    Photo p = photos.get(getAdapterPosition());
                    p.setSelected(true);
                    notifyItemChanged(getAdapterPosition());
                    photoListener.onPhotoLongClick();
                    photoListener.onSelectedPhotoClick(p);
                    return true;
                }
                return false;
            });
        }
    }
}
