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
import com.squareup.picasso.Picasso;

import java.io.File;
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
        PhotoDiffUtil diffUtilCallback = new PhotoDiffUtil(items, mPhotoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback);
        mPhotoList.clear();
        mPhotoList.addAll(items);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        private int targetHeight;
        private int targetWidth;
        private ImageView mPhoto;

        UploadViewHolder(View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.image_photo);
            targetWidth = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_min_width);
            targetHeight = itemView.getContext().getResources().getDimensionPixelOffset(R.dimen.photo_min_height);
        }

        void bind(Photo photo) {
            Picasso.get()
                    .load(new File(photo.getLocalPath()))
                    .resize(targetWidth, targetHeight)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(mPhoto);
        }
    }

}