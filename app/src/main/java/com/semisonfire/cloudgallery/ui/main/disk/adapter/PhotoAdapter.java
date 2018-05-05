package com.semisonfire.cloudgallery.ui.main.disk.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.semisonfire.cloudgallery.R;
import com.semisonfire.cloudgallery.data.model.Photo;
import com.semisonfire.cloudgallery.ui.custom.PhotoDiffUtil;
import com.semisonfire.cloudgallery.ui.custom.SelectableHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
        PhotoDiffUtil diffUtilCallback = new PhotoDiffUtil(items, mPhotoList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtilCallback);
        mPhotoList.clear();
        mPhotoList.addAll(items);
        diffResult.dispatchUpdatesTo(this);
        /*
        notifyDataSetChanged();*/
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
        private ImageView mSelect;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.image_photo);
            mSelect = itemView.findViewById(R.id.image_selected);
        }

        void bind(Photo photo) {

            View.OnClickListener onItemClick = v -> {
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
            };

            mPhoto.setImageDrawable(null);
            Picasso.get().load(photo.getPreview())
                    .resize(600, 600)
                    .centerCrop()
                    .placeholder(R.drawable.empty)
                    .into(mPhoto);

            mPhoto.setOnClickListener(onItemClick);
            mSelect.setOnClickListener(onItemClick);
            mSelect.setVisibility(photo.isSelected() ? View.VISIBLE : View.GONE);

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

        private void select(Bitmap bitmap) {

            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();
            paint.setColor(Color.TRANSPARENT);
            paint.setStyle(Paint.Style.FILL);
            float left = 50;
            float top =  50;
            float right =  150;
            float bottom =  150;
            canvas.drawRect(left, top, right, bottom, paint);

            paint.setStrokeWidth(10);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}
