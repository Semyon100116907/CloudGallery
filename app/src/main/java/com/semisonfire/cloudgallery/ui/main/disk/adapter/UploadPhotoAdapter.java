package com.semisonfire.cloudgallery.ui.main.disk.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UploadPhotoAdapter extends RecyclerView.Adapter<UploadPhotoAdapter.UploadViewHolder> {

    private List<Photo> mPhotoList;

    public UploadPhotoAdapter() {
        mPhotoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new UploadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        holder.bind(mPhotoList.get(position));
    }

    public void setItems(List<Photo> items) {
        mPhotoList.clear();
        mPhotoList.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(Photo item) {
        mPhotoList.add(item);
        notifyItemInserted(mPhotoList.size());
    }

    public void removeItem(Photo item) {
        mPhotoList.remove(item);
        notifyItemRemoved(0);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        int targetHeight;
        int targetWidth;
        private ImageView mPhoto;

        UploadViewHolder(View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.image_photo);
            targetWidth = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_min_width);
            targetHeight = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_min_height);
        }

        void bind(Photo photo) {
            Picasso.get()
                    .load(photo.getPreview())
                    .resize(targetWidth, targetHeight)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(mPhoto);
        }
    }

}
