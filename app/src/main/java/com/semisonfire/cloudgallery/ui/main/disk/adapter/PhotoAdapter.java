package com.semisonfire.cloudgallery.ui.main.disk.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private SelectableHelper.OnPhotoListener mPhotoClickListener;
    private List<Photo> mPhotoList;

    public PhotoAdapter(SelectableHelper.OnPhotoListener clickListener) {
        mPhotoClickListener = clickListener;
        mPhotoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(mPhotoList.get(position));
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    public void setPhotos(List<Photo> items) {
        mPhotoList.clear();
        mPhotoList.addAll(items);
        notifyDataSetChanged();
    }

    public void addPhoto(Photo item) {
        mPhotoList.add(item);
        notifyItemInserted(getItemCount());
    }

    public void addPhotos(List<Photo> photos) {
        mPhotoList.addAll(photos);
        notifyItemRangeInserted(getItemCount(), photos.size());
    }

    public void setSelection(boolean selected) {
        if (!selected) {
            for (Photo photo : mPhotoList) {
                if (photo.isSelected()) {
                    photo.setSelected(false);
                }
            }
        }
        SelectableHelper.setMultipleSelection(selected);
        notifyDataSetChanged();
    }

    public void remove(Photo photo) {
        int position = mPhotoList.indexOf(photo);
        mPhotoList.remove(photo);
        notifyItemRemoved(position);
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPhoto;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.image_photo);
        }

        void bind(Photo photo) {
            mPhoto.setImageDrawable(null);
            if (photo.isSelected()) {
                Picasso.get().load(R.drawable.empty)
                        .resize(600, 600)
                        .centerCrop()
                        .placeholder(R.drawable.empty)
                        .into(mPhoto);
            } else {
                Picasso.get().load(photo.getPreview())
                        .resize(600, 600)
                        .centerCrop()
                        .placeholder(R.drawable.empty)
                        .into(mPhoto);
            }

            mPhoto.setOnClickListener(v -> {
                if (mPhotoClickListener != null) {
                    Photo p = mPhotoList.get(getAdapterPosition());
                    if (!SelectableHelper.getMultipleSelection()) {
                        mPhotoClickListener.onPhotoClick(mPhotoList, getAdapterPosition());
                    } else {
                        p.setSelected(!p.isSelected());
                        notifyItemChanged(getAdapterPosition());
                        mPhotoClickListener.onSelectedPhotoClick(p);
                    }
                }
            });

            mPhoto.setOnLongClickListener(v -> {
                if (mPhotoClickListener != null && !SelectableHelper.getMultipleSelection()) {
                    Photo p = mPhotoList.get(getAdapterPosition());
                    p.setSelected(true);
                    notifyItemChanged(getAdapterPosition());
                    mPhotoClickListener.onPhotoLongClick();
                    mPhotoClickListener.onSelectedPhotoClick(p);
                    return true;
                }
                return false;
            });
        }
    }
}
